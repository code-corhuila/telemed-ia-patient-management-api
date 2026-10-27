package com.telemedai.patient.infrastructure.config.security;

import java.util.UUID;

/**
 * Represents the authenticated user identity as extracted from the JWT.
 *
 * The `userId` is the Identity `sub` claim (a UUID), which is also the
 * external reference used by `patients.user_id` in this bounded context.
 */
public record AuthenticatedUser(UUID userId, String role) {

    public boolean isPatient() {
        return "PATIENT".equals(role);
    }
}
