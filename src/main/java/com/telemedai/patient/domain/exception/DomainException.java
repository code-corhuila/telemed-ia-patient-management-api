package com.telemedai.patient.domain.exception;

/**
 * Base class for all domain-level exceptions in the Patient Management context.
 *
 * Domain exceptions represent violations of business invariants. They must not
 * depend on any infrastructure or framework type.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
