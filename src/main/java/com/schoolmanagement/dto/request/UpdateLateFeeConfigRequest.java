package com.schoolmanagement.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateLateFeeConfigRequest(
        @NotNull @Min(0) BigDecimal amountPerDay,
        @NotNull @Min(0) Integer gracePeriodDays
) {}
