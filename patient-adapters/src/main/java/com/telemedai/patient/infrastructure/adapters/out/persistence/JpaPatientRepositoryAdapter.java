package com.telemedai.patient.infrastructure.adapters.out.persistence;

import com.telemedai.patient.application.ports.out.PatientRepositoryPort;
import com.telemedai.patient.domain.model.Patient;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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
    public Optional<Patient> findByUserId(Long userId) {
        return repository.findByUserIdAndDeletedAtIsNull(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<Patient> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Patient> findAllActive(int offset, int limit) {
        int page = offset / Math.max(limit, 1);
        return repository.findActive(PageRequest.of(page, limit)).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countActive() {
        return repository.countByDeletedAtIsNull();
    }
}