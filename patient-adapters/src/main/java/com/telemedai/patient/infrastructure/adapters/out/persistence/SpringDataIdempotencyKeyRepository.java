package com.telemedai.patient.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataIdempotencyKeyRepository extends JpaRepository<JpaIdempotencyKeyEntity, String> {
}