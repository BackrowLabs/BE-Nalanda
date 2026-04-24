package com.schoolmanagement.dto.response;

import com.schoolmanagement.enums.Gender;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String fullName,
        String designation,
        String department,
        String phone,
        String email,
        LocalDate dateOfBirth,
        Gender gender,
        String address,
        LocalDate joinDate,
        BigDecimal monthlySalary,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {}
