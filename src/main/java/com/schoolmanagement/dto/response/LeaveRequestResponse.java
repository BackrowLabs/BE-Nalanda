package com.schoolmanagement.dto.response;

import com.schoolmanagement.enums.LeaveStatus;
import com.schoolmanagement.enums.LeaveType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record LeaveRequestResponse(
        Long id,
        Long employeeId,
        String employeeName,
        String employeeCode,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        long days,
        String reason,
        LeaveStatus status,
        String approvedByName,
        Instant approvedAt,
        String remarks,
        Instant createdAt
) {
    public static long calculateDays(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end) + 1;
    }
}
