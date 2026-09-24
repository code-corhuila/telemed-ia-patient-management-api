package com.telemedai.patient.application.dto;

import com.telemedai.patient.domain.model.Patient;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Application-level view of a patient profile.
 * Returned by the use cases and consumed by the REST layer.
 */
public record PatientResponse(
        Long id,
        Long userId,
        LocalDate birthDate,
        String phone,
        String medicalHistory,
        String description,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getUserId(),
                patient.getBirthDate(),
                patient.getPhone(),
                patient.getMedicalHistory(),
                patient.getDescription(),
                patient.getCreatedAt(),
                patient.getUpdatedAt()
        );
    }
}
