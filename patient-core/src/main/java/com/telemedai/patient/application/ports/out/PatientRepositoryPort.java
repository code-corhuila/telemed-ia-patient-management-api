package com.telemedai.patient.application.ports.out;

import com.telemedai.patient.domain.model.Patient;

import java.util.List;
import java.util.Optional;

public interface PatientRepositoryPort {

    Patient save(Patient patient);

    Optional<Patient> findByUserId(Long userId);

    Optional<Patient> findById(Long id);

    List<Patient> findAllActive(int offset, int limit);

    long countActive();
}