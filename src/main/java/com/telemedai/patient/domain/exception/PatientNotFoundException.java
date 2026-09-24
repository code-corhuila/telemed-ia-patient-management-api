package com.telemedai.patient.domain.exception;

/**
 * Thrown when a patient cannot be found for a given user identifier.
 */
public class PatientNotFoundException extends DomainException {

    public PatientNotFoundException(Long userId) {
        super("Patient not found for userId: " + userId);
    }
}