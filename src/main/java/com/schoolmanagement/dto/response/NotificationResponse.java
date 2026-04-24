package com.schoolmanagement.dto.response;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String message,
        Long relatedEntityId,
        boolean read,
        String createdByName,
        Instant createdAt
) {}
