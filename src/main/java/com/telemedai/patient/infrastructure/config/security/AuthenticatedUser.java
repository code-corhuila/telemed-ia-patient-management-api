package com.telemedai.patient.infrastructure.config.security;

/**
 * Represents the authenticated user identity as extracted from the trusted
 * headers set by the API Gateway.
 *
 * The API Gateway validates the JWT and injects `X-User-Id` and `X-User-Role`.
 * Direct access to this service is rejected by the `GatewayTrustFilter` unless
 * a valid shared secret header is present.
 */
public record AuthenticatedUser(Long userId, String role) {

    public boolean isPatient() {
        return "PATIENT".equals(role);
    }
}
