package com.schoolmanagement.dto.response;

import com.schoolmanagement.enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record EmployeeAttendanceResponse(
        Long id,
        Long employeeId,
        String employeeName,
        String employeeCode,
        LocalDate date,
        AttendanceStatus status,
        LocalTime checkIn,
        LocalTime checkOut,
        String remarks
) {}
