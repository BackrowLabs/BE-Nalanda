package com.schoolmanagement.dto.request;

import com.schoolmanagement.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateEmployeeRequest(
        @NotBlank String fullName,
        @NotNull BigDecimal monthlySalary,
        String designation,
        String department,
        String phone,
        String email,
        LocalDate dateOfBirth,
        Gender gender,
        String address,
        LocalDate joinDate
) {}
