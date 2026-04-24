package com.schoolmanagement.dto.response;

import com.schoolmanagement.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record FeePaymentResponse(
        Long id,
        Long studentFeeId,
        Long studentId,
        String studentName,
        String admissionNumber,
        String sectionName,
        String feeType,
        int installmentNumber,
        BigDecimal amount,
        LocalDate paymentDate,
        String receiptNumber,
        String collectedByName,
        PaymentStatus status,
        String approvedByName,
        Instant approvedAt,
        String rejectionReason,
        String notes,
        Instant createdAt
) {}
