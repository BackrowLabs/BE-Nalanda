package com.schoolmanagement.service;

import com.schoolmanagement.dto.request.CreateFeeStructureRequest;
import com.schoolmanagement.dto.request.UpdateFeeStructureRequest;
import com.schoolmanagement.dto.response.FeeStructureResponse;
import com.schoolmanagement.dto.response.StudentFeeResponse;
import com.schoolmanagement.entity.*;
import com.schoolmanagement.exception.BusinessException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.enums.PaymentStatus;
import com.schoolmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeeStructureService {

    private final FeeStructureRepository feeStructureRepository;
    private final FeeInstallmentRepository feeInstallmentRepository;
    private final StudentFeeRepository studentFeeRepository;
    private final StudentRepository studentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final GradeRepository gradeRepository;
    private final LateFeeConfigRepository lateFeeConfigRepository;
    private final FeePaymentRepository feePaymentRepository;

    public List<FeeStructureResponse> getAll(Long yearId, Long gradeId) {
        return feeStructureRepository.findAllFiltered(yearId, gradeId)
                .stream().map(this::toResponse).toList();
    }

    public FeeStructureResponse getById(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public FeeStructureResponse create(CreateFeeStructureRequest req) {
        if (feeStructureRepository.existsByAcademicYearIdAndGradeIdAndFeeType(
                req.academicYearId(), req.gradeId(), req.feeType())) {
            throw new BusinessException("Fee structure already exists for this grade, year and fee type");
        }

        var year = academicYearRepository.findById(req.academicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year", req.academicYearId()));
        var grade = gradeRepository.findById(req.gradeId())
                .orElseThrow(() -> new ResourceNotFoundException("Grade", req.gradeId()));

        FeeStructure structure = FeeStructure.builder()
                .academicYear(year)
                .grade(grade)
                .feeType(req.feeType())
                .totalAmount(req.totalAmount())
                .build();

        structure = feeStructureRepository.save(structure);

        for (var item : req.installments()) {
            FeeInstallment installment = FeeInstallment.builder()
                    .feeStructure(structure)
                    .installmentNumber(item.installmentNumber())
                    .dueDate(item.dueDate())
                    .amount(item.amount())
                    .build();
            structure.getInstallments().add(feeInstallmentRepository.save(installment));
        }

        publishToStudents(structure.getId());

        return toResponse(structure);
    }

    @Transactional
    public FeeStructureResponse update(Long id, UpdateFeeStructureRequest req) {
        FeeStructure fs = find(id);
        fs.setTotalAmount(req.totalAmount());

        Map<Integer, FeeInstallment> existingByNumber = fs.getInstallments().stream()
                .collect(Collectors.toMap(FeeInstallment::getInstallmentNumber, i -> i));

        Set<Integer> requestedNumbers = req.installments().stream()
                .map(UpdateFeeStructureRequest.InstallmentItem::installmentNumber)
                .collect(Collectors.toSet());

        // Remove installments not in request (guard against published fees)
        Iterator<FeeInstallment> it = fs.getInstallments().iterator();
        while (it.hasNext()) {
            FeeInstallment inst = it.next();
            if (!requestedNumbers.contains(inst.getInstallmentNumber())) {
                if (studentFeeRepository.existsByFeeInstallmentId(inst.getId())) {
                    throw new BusinessException(
                            "Cannot remove installment #" + inst.getInstallmentNumber()
                            + " — it already has student fee records.");
                }
                it.remove();
            }
        }

        // Update existing or add new installments
        for (var item : req.installments()) {
            if (existingByNumber.containsKey(item.installmentNumber())) {
                FeeInstallment existing = existingByNumber.get(item.installmentNumber());
                existing.setDueDate(item.dueDate());
                existing.setAmount(item.amount());
            } else {
                FeeInstallment newInst = FeeInstallment.builder()
                        .feeStructure(fs)
                        .installmentNumber(item.installmentNumber())
                        .dueDate(item.dueDate())
                        .amount(item.amount())
                        .build();
                fs.getInstallments().add(newInst);
            }
        }

        return toResponse(feeStructureRepository.save(fs));
    }

    @Transactional
    public int publishToStudents(Long feeStructureId) {
        FeeStructure structure = find(feeStructureId);
        if (structure.getInstallments().isEmpty()) {
            throw new BusinessException("No installments defined for this fee structure");
        }

        var students = studentRepository.search(
                structure.getAcademicYear().getId(),
                null, null,
                null,
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).stream()
                .filter(s -> s.getSection() != null
                        && s.getSection().getGrade().getId().equals(structure.getGrade().getId()))
                .toList();

        int count = 0;
        for (Student student : students) {
            for (FeeInstallment installment : structure.getInstallments()) {
                if (!studentFeeRepository.existsByStudentIdAndFeeInstallmentId(
                        student.getId(), installment.getId())) {
                    studentFeeRepository.save(StudentFee.builder()
                            .student(student)
                            .feeInstallment(installment)
                            .amountDue(installment.getAmount())
                            .build());
                    count++;
                }
            }
        }
        return count;
    }

    public List<StudentFeeResponse> getStudentFees(Long studentId) {
        LateFeeConfig config = lateFeeConfigRepository.findAll().stream().findFirst().orElse(null);
        return studentFeeRepository.findByStudentId(studentId)
                .stream().map(sf -> toStudentFeeResponse(sf, config)).toList();
    }

    public List<StudentFeeResponse> getInstallmentFees(Long installmentId) {
        LateFeeConfig config = lateFeeConfigRepository.findAll().stream().findFirst().orElse(null);
        return studentFeeRepository.findByInstallmentId(installmentId)
                .stream().map(sf -> toStudentFeeResponse(sf, config)).toList();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private FeeStructure find(Long id) {
        return feeStructureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure", id));
    }

    private BigDecimal calculateLateFee(FeeInstallment installment, LateFeeConfig config) {
        if (config == null || config.getAmountPerDay().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        LocalDate today = LocalDate.now();
        long daysOverdue = ChronoUnit.DAYS.between(installment.getDueDate(), today);
        long chargeable = daysOverdue - config.getGracePeriodDays();
        if (chargeable <= 0) return BigDecimal.ZERO;
        return config.getAmountPerDay().multiply(BigDecimal.valueOf(chargeable));
    }

    private StudentFeeResponse toStudentFeeResponse(StudentFee sf, LateFeeConfig config) {
        var fi = sf.getFeeInstallment();
        var fs = fi.getFeeStructure();
        var student = sf.getStudent();

        BigDecimal lateFee = calculateLateFee(fi, config);
        BigDecimal balance = sf.getAmountDue().add(lateFee).subtract(sf.getAmountPaid());
        boolean overdue = LocalDate.now().isAfter(fi.getDueDate())
                && sf.getStatus() != com.schoolmanagement.enums.StudentFeeStatus.PAID;

        var approvedPayment = feePaymentRepository
                .findFirstByStudentFeeIdAndStatusOrderByCreatedAtDesc(sf.getId(), PaymentStatus.APPROVED)
                .orElse(null);

        var rejectedPayment = feePaymentRepository
                .findFirstByStudentFeeIdAndStatusOrderByCreatedAtDesc(sf.getId(), PaymentStatus.REJECTED)
                .orElse(null);

        return new StudentFeeResponse(
                sf.getId(),
                student.getId(),
                student.getFullName(),
                student.getAdmissionNumber(),
                student.getSection() != null ? student.getSection().getFullName() : null,
                fi.getId(),
                fi.getInstallmentNumber(),
                fi.getDueDate(),
                fs.getFeeType(),
                sf.getAmountDue(),
                sf.getAmountPaid(),
                lateFee,
                balance,
                sf.getStatus(),
                overdue,
                approvedPayment != null ? approvedPayment.getId() : null,
                approvedPayment != null ? approvedPayment.getReceiptNumber() : null,
                rejectedPayment != null ? rejectedPayment.getId() : null,
                rejectedPayment != null ? rejectedPayment.getRejectionReason() : null
        );
    }

    private FeeStructureResponse toResponse(FeeStructure fs) {
        var installments = fs.getInstallments().stream()
                .map(i -> new FeeStructureResponse.InstallmentResponse(
                        i.getId(), i.getInstallmentNumber(), i.getDueDate(), i.getAmount()))
                .toList();
        return new FeeStructureResponse(
                fs.getId(),
                fs.getAcademicYear().getId(),
                fs.getAcademicYear().getName(),
                fs.getGrade().getId(),
                fs.getGrade().getName(),
                fs.getFeeType(),
                fs.getTotalAmount(),
                installments,
                fs.getCreatedAt()
        );
    }
}
