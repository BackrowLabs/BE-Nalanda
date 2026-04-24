package com.schoolmanagement.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UpdateFeeStructureRequest(
        @NotNull @Positive BigDecimal totalAmount,
        @NotEmpty @Valid List<InstallmentItem> installments
) {
    public record InstallmentItem(
            @NotNull Integer installmentNumber,
            @NotNull LocalDate dueDate,
            @NotNull @Positive BigDecimal amount
    ) {}
}
