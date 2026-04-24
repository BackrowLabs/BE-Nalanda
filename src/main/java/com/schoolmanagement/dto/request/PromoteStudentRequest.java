package com.schoolmanagement.dto.request;

import jakarta.validation.constraints.NotNull;

public record PromoteStudentRequest(
        @NotNull Long sectionId,
        @NotNull Long academicYearId
) {}
