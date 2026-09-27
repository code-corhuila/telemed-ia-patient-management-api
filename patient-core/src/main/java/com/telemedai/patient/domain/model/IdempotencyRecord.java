package com.telemedai.patient.domain.model;

import java.time.OffsetDateTime;
import java.util.Objects;

public record IdempotencyRecord(
        String key,
        Long userId,
        Long resourceId,
        OffsetDateTime createdAt
) {
    public IdempotencyRecord {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(resourceId, "resourceId");
    }
}