package com.schoolmanagement.dto.request;

import com.schoolmanagement.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record EmployeeAttendanceRequest(
        @NotNull Long employeeId,
        @NotNull LocalDate date,
        @NotNull AttendanceStatus status,
        LocalTime checkIn,
        LocalTime checkOut,
        String remarks
) {}
