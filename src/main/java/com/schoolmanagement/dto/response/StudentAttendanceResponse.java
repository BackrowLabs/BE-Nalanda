package com.schoolmanagement.dto.response;

import com.schoolmanagement.enums.AttendanceStatus;

import java.time.LocalDate;

public record StudentAttendanceResponse(
        Long id,
        Long studentId,
        String studentName,
        String admissionNumber,
        LocalDate date,
        AttendanceStatus status,
        String remarks
) {}
