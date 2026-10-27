package com.telemedai.patient.application.ports.out;

import com.telemedai.patient.domain.model.Patient;

import java.util.Optional;
import java.util.UUID;

public interface PatientRepositoryPort {

    Patient save(Patient patient);

    Optional<Patient> findByUserId(UUID userId);

    Optional<Patient> findById(UUID id);
}
