package com.telemedai.patient.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreatePatientCommand(
        UUID userId,
        LocalDate birthDate,
        String phone,
        String medicalHistory,
        String description
) {
}
