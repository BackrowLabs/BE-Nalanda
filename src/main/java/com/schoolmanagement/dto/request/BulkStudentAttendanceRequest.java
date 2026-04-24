package com.schoolmanagement.dto.request;

import com.schoolmanagement.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record BulkStudentAttendanceRequest(
        @NotNull Long sectionId,
        @NotNull Long academicYearId,
        @NotNull LocalDate date,
        @NotNull List<AttendanceEntry> entries
) {
    public record AttendanceEntry(
            @NotNull Long studentId,
            @NotNull AttendanceStatus status,
            String remarks
    ) {}
}
