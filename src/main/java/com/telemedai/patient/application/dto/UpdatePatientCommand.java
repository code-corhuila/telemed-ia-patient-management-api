package com.telemedai.patient.application.dto;

import java.time.LocalDate;

/**
 * Input command for the UpdatePatientProfile use case.
 * Contains the editable fields of the patient profile.
 */
public record UpdatePatientCommand(
        Long userId,
        LocalDate birthDate,
        String phone,
        String medicalHistory,
        String description
) {
}
