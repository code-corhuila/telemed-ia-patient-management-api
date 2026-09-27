package com.telemedai.patient.application.ports.out;

import com.telemedai.patient.domain.model.IdempotencyRecord;

import java.util.Optional;

public interface IdempotencyKeyRepositoryPort {

    Optional<IdempotencyRecord> findByKey(String key);

    IdempotencyRecord save(IdempotencyRecord record);
}