package com.telemedai.patient.domain.model;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record IdempotencyRecord(
        String key,
        UUID userId,
        UUID resourceId,
        OffsetDateTime createdAt
) {
    public IdempotencyRecord {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(resourceId, "resourceId");
    }
}
