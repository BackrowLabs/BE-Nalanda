package com.schoolmanagement.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record GradeRecordResponse(
        Long id,
        Long studentId,
        String studentName,
        String admissionNumber,
        String sectionName,
        Long academicYearId,
        String academicYearName,
        String subject,
        String term,
        String letterGrade,
        BigDecimal marks,
        String remarks,
        Instant updatedAt
) {}
