package com.schoolmanagement.dto.response;

public record StudentSummaryResponse(
        Long id,
        String admissionNumber,
        String fullName,
        String sectionName,
        String academicYearName,
        String parentPhone,
        boolean isActive
) {}
