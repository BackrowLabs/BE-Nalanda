package com.schoolmanagement.dto.response;

import com.schoolmanagement.enums.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        Role role,
        boolean isActive,
        Instant createdAt
) {}
