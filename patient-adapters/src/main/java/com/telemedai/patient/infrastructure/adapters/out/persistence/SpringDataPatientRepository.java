package com.telemedai.patient.infrastructure.adapters.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

interface SpringDataPatientRepository extends JpaRepository<JpaPatientEntity, Long> {

    Optional<JpaPatientEntity> findByUserIdAndDeletedAtIsNull(Long userId);

    @Query("select p from JpaPatientEntity p where p.deletedAt is null order by p.createdAt desc")
    List<JpaPatientEntity> findActive(Pageable pageable);

    long countByDeletedAtIsNull();
}