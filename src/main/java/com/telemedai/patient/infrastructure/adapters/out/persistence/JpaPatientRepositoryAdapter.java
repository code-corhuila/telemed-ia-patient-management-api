package com.telemedai.patient.infrastructure.adapters.out.persistence;

import com.telemedai.patient.application.ports.out.PatientRepositoryPort;
import com.telemedai.patient.domain.model.Patient;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Persistence adapter that implements the application output port
 * `PatientRepositoryPort` using Spring Data JPA.
 *
 * This is the only place in the codebase where the domain aggregate is
 * translated to and from a JPA entity.
 */
@Component
class JpaPatientRepositoryAdapter implements PatientRepositoryPort {

    private final SpringDataPatientRepository repository;
    private final PatientMapper mapper;

    JpaPatientRepositoryAdapter(SpringDataPatientRepository repository,
                                PatientMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Patient save(Patient patient) {
        JpaPatientEntity entity = mapper.toEntity(patient);
        JpaPatientEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Patient> findByUserId(Long userId) {
        return repository.findByUserId(userId).map(mapper::toDomain);
    }
}
