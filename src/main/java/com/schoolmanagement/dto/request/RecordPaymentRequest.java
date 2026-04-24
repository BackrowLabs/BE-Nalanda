package com.schoolmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordPaymentRequest(
        @NotNull Long studentFeeId,
        @NotNull @Positive BigDecimal amount,
        @NotNull LocalDate paymentDate,
        String notes
) {}
