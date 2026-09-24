package com.telemedai.patient.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the `patients` table.
 *
 * This interface is package-private infrastructure: it is only used by the
 * persistence adapter and never exposed to the application or domain layers.
 */
interface SpringDataPatientRepository extends JpaRepository<JpaPatientEntity, Long> {

    Optional<JpaPatientEntity> findByUserId(Long userId);
}
