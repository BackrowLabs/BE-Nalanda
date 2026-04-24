package com.schoolmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record SaveGradeRequest(
        @NotNull Long academicYearId,
        @NotNull Long sectionId,
        @NotBlank String subject,
        @NotBlank String term,
        @NotNull List<GradeEntry> entries
) {
    public record GradeEntry(
            @NotNull Long studentId,
            String letterGrade,
            BigDecimal marks,
            String remarks
    ) {}
}
