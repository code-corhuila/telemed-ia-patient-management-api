package com.telemedai.patient.infrastructure.adapters.out.persistence;

import com.telemedai.patient.application.ports.out.IdempotencyKeyRepositoryPort;
import com.telemedai.patient.domain.model.IdempotencyRecord;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaIdempotencyKeyRepositoryAdapter implements IdempotencyKeyRepositoryPort {

    private final SpringDataIdempotencyKeyRepository repository;

    JpaIdempotencyKeyRepositoryAdapter(SpringDataIdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<IdempotencyRecord> findByKey(String key) {
        return repository.findById(key).map(e -> new IdempotencyRecord(
                e.getKey(), e.getUserId(), e.getResourceId(), e.getCreatedAt()));
    }

    @Override
    public IdempotencyRecord save(IdempotencyRecord record) {
        JpaIdempotencyKeyEntity entity = new JpaIdempotencyKeyEntity();
        entity.setKey(record.key());
        entity.setUserId(record.userId());
        entity.setResourceId(record.resourceId());
        entity.setCreatedAt(record.createdAt());
        JpaIdempotencyKeyEntity saved = repository.save(entity);
        return new IdempotencyRecord(saved.getKey(), saved.getUserId(),
                saved.getResourceId(), saved.getCreatedAt());
    }
}