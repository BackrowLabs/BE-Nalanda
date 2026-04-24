package com.schoolmanagement.dto.response;

import com.schoolmanagement.enums.FeeType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record FeeStructureResponse(
        Long id,
        Long academicYearId,
        String academicYearName,
        Long gradeId,
        String gradeName,
        FeeType feeType,
        BigDecimal totalAmount,
        List<InstallmentResponse> installments,
        Instant createdAt
) {
    public record InstallmentResponse(
            Long id,
            int installmentNumber,
            LocalDate dueDate,
            BigDecimal amount
    ) {}
}
