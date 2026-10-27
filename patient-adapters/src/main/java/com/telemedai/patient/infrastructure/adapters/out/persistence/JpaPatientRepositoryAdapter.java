package com.telemedai.patient.infrastructure.adapters.out.persistence;

import com.telemedai.patient.application.ports.out.PatientRepositoryPort;
import com.telemedai.patient.domain.model.Patient;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JpaPatientRepositoryAdapter implements PatientRepositoryPort {

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
    public Optional<Patient> findByUserId(UUID userId) {
        return repository.findByUserIdAndDeletedAtIsNull(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<Patient> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
}
