package com.telemedai.patient.infrastructure.adapters.out.persistence;

import com.telemedai.patient.domain.model.Patient;
import org.springframework.stereotype.Component;

/**
 * Translates between the domain aggregate (`Patient`) and the JPA entity
 * (`JpaPatientEntity`).
 *
 * The mapper lives in the infrastructure layer because it knows about both
 * sides of the boundary. The domain stays unaware of JPA.
 */
@Component
class PatientMapper {

    Patient toDomain(JpaPatientEntity entity) {
        return Patient.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getBirthDate(),
                entity.getPhone(),
                entity.getMedicalHistory(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    JpaPatientEntity toEntity(Patient patient) {
        JpaPatientEntity entity = new JpaPatientEntity();
        entity.setId(patient.getId());
        entity.setUserId(patient.getUserId());
        entity.setBirthDate(patient.getBirthDate());
        entity.setPhone(patient.getPhone());
        entity.setMedicalHistory(patient.getMedicalHistory());
        entity.setDescription(patient.getDescription());
        entity.setDeletedAt(patient.getDeletedAt());
        return entity;
    }
}
