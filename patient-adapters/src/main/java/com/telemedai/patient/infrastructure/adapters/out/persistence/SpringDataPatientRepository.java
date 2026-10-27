package com.telemedai.patient.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataPatientRepository extends JpaRepository<JpaPatientEntity, UUID> {

    Optional<JpaPatientEntity> findByUserIdAndDeletedAtIsNull(UUID userId);
}
