package com.schoolmanagement.dto.response;

import com.schoolmanagement.enums.Gender;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record StudentResponse(
        Long id,
        String admissionNumber,
        String fullName,
        LocalDate dateOfBirth,
        Gender gender,
        String address,
        String photoUrl,
        String parentName,
        String parentPhone,
        String parentEmail,
        Long sectionId,
        String sectionName,
        Long academicYearId,
        String academicYearName,
        LocalDate enrollmentDate,
        boolean isActive,
        List<DocumentResponse> documents,
        Instant createdAt,
        Instant updatedAt
) {
    public record DocumentResponse(Long id, String name, String url, Instant uploadedAt) {}
}
