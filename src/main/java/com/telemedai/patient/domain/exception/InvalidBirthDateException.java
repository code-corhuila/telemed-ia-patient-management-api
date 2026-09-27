package com.telemedai.patient.domain.exception;

/**
 * Thrown when a patient's birth date violates the domain rule that it must not
 * be in the future.
 */
public class InvalidBirthDateException extends DomainException {

    public InvalidBirthDateException(String message) {
        super(message);
    }
}
