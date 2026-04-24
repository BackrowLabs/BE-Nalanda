package com.schoolmanagement.dto.request;

import com.schoolmanagement.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateStudentRequest(
        @NotBlank String fullName,
        @NotNull Long sectionId,
        LocalDate dateOfBirth,
        Gender gender,
        String address,
        String photoUrl,
        String parentName,
        String parentPhone,
        String parentEmail
) {}
