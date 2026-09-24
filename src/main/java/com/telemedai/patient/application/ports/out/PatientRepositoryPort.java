package com.telemedai.patient.application.ports.out;

import com.telemedai.patient.domain.model.Patient;

import java.util.Optional;

/**
 * Output port for patient persistence.
 * Implemented by an infrastructure adapter (JPA, in-memory, etc.).
 */
public interface PatientRepositoryPort {

    /**
     * Persists the given patient.
     *
     * @param patient the aggregate to save
     * @return the persisted aggregate (with any generated identifiers populated)
     */
    Patient save(Patient patient);

    /**
     * Retrieves a patient by the external user identifier.
     *
     * @param userId the user identifier
     * @return the patient if present
     */
    Optional<Patient> findByUserId(Long userId);
}
