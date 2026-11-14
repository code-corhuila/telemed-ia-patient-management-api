package com.telemedai.patient.application.ports.out;

import com.telemedai.patient.domain.exception.DomainException;

import java.util.UUID;

/**
 * Thrown when a patient cannot be found for a given user identifier.
 */
public class PatientNotFoundException extends DomainException {

    public PatientNotFoundException(UUID userId) {
        super("Patient not found for userId: " + userId);
    }
}
