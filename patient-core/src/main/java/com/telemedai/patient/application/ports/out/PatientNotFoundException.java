package com.telemedai.patient.application.ports.out;

import com.telemedai.patient.domain.exception.DomainException;

/**
 * Thrown when a patient cannot be found for a given user identifier.
 */
public class PatientNotFoundException extends DomainException {

    public PatientNotFoundException(Long userId) {
        super("Patient not found for userId: " + userId);
    }
}