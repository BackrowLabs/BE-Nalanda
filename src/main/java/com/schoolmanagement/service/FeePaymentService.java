package com.schoolmanagement.service;

import com.schoolmanagement.dto.request.ApprovePaymentRequest;
import com.schoolmanagement.dto.request.RecordPaymentRequest;
import com.schoolmanagement.dto.response.FeePaymentResponse;
import com.schoolmanagement.dto.response.PageResponse;
import com.schoolmanagement.entity.*;
import com.schoolmanagement.enums.PaymentStatus;
import com.schoolmanagement.enums.StudentFeeStatus;
import com.schoolmanagement.exception.BusinessException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeePaymentService {

    private final FeePaymentRepository feePaymentRepository;
    private final StudentFeeRepository studentFeeRepository;
    private final PdfService pdfService;

    public PageResponse<FeePaymentResponse> getPendingPayments(int page, int size) {
        var result = feePaymentRepository.findByStatus(
                PaymentStatus.PENDING_APPROVAL, PageRequest.of(page, size));
        return PageResponse.of(result, this::toResponse);
    }

    public long countPendingApprovals() {
        return feePaymentRepository.countByStatus(PaymentStatus.PENDING_APPROVAL);
    }

    @Transactional
    public FeePaymentResponse recordPayment(RecordPaymentRequest req) {
        StudentFee studentFee = studentFeeRepository.findById(req.studentFeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Student fee", req.studentFeeId()));

        if (studentFee.getStatus() == StudentFeeStatus.PAID) {
            throw new BusinessException("This installment is already fully paid");
        }

        Profile collector = currentProfile();

        FeePayment payment = FeePayment.builder()
                .studentFee(studentFee)
                .amount(req.amount())
                .paymentDate(req.paymentDate())
                .collectedBy(collector)
                .notes(req.notes())
                .build();

        return toResponse(feePaymentRepository.save(payment));
    }

    @Transactional
    public FeePaymentResponse approveOrReject(Long paymentId, ApprovePaymentRequest req) {
        FeePayment payment = feePaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING_APPROVAL) {
            throw new BusinessException("Payment is not pending approval");
        }

        Profile approver = currentProfile();
        payment.setApprovedBy(approver);
        payment.setApprovedAt(Instant.now());

        if (req.approved()) {
            payment.setStatus(PaymentStatus.APPROVED);
            payment.setReceiptNumber(generateReceiptNumber(payment.getId()));

            // Update student fee balance
            StudentFee sf = payment.getStudentFee();
            sf.setAmountPaid(sf.getAmountPaid().add(payment.getAmount()));

            var total = sf.getAmountDue().add(sf.getLateFee());
            if (sf.getAmountPaid().compareTo(total) >= 0) {
                sf.setStatus(StudentFeeStatus.PAID);
            } else {
                sf.setStatus(StudentFeeStatus.PARTIAL);
            }
            studentFeeRepository.save(sf);
        } else {
            if (req.rejectionReason() == null || req.rejectionReason().isBlank()) {
                throw new BusinessException("Rejection reason is required");
            }
            payment.setStatus(PaymentStatus.REJECTED);
            payment.setRejectionReason(req.rejectionReason());
        }

        return toResponse(feePaymentRepository.save(payment));
    }

    public byte[] generateReceiptPdf(Long paymentId) {
        FeePayment payment = feePaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if (payment.getStatus() != PaymentStatus.APPROVED) {
            throw new BusinessException("Receipt is only available for approved payments");
        }

        return pdfService.generateReceipt(payment);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Profile currentProfile() {
        return (Profile) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String generateReceiptNumber(Long paymentId) {
        return String.format("RCPT-%d-%06d", Year.now().getValue(), paymentId);
    }

    private FeePaymentResponse toResponse(FeePayment p) {
        var sf = p.getStudentFee();
        var fi = sf.getFeeInstallment();
        var student = sf.getStudent();

        return new FeePaymentResponse(
                p.getId(),
                sf.getId(),
                student.getId(),
                student.getFullName(),
                student.getAdmissionNumber(),
                student.getSection() != null ? student.getSection().getFullName() : null,
                fi.getFeeStructure().getFeeType().name(),
                fi.getInstallmentNumber(),
                p.getAmount(),
                p.getPaymentDate(),
                p.getReceiptNumber(),
                p.getCollectedBy().getFullName(),
                p.getStatus(),
                p.getApprovedBy() != null ? p.getApprovedBy().getFullName() : null,
                p.getApprovedAt(),
                p.getRejectionReason(),
                p.getNotes(),
                p.getCreatedAt()
        );
    }
}
