package com.schoolmanagement.dto.response;

import com.schoolmanagement.enums.FeeType;
import com.schoolmanagement.enums.StudentFeeStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StudentFeeResponse(
        Long id,
        Long studentId,
        String studentName,
        String admissionNumber,
        String sectionName,
        Long installmentId,
        int installmentNumber,
        LocalDate dueDate,
        FeeType feeType,
        BigDecimal amountDue,
        BigDecimal amountPaid,
        BigDecimal lateFee,
        BigDecimal balance,
        StudentFeeStatus status,
        boolean overdue,
        Long approvedPaymentId,
        String receiptNumber,
        Long rejectedPaymentId,
        String rejectedReason
) {}
