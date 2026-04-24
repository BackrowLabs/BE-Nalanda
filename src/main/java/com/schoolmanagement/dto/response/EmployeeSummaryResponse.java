package com.schoolmanagement.dto.response;

public record EmployeeSummaryResponse(
        Long id,
        String employeeCode,
        String fullName,
        String designation,
        String department,
        String phone,
        boolean isActive
) {}
