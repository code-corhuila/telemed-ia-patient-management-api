package com.telemedai.patient.application.dto;

import java.time.LocalDate;

public record CreatePatientCommand(
        Long userId,
        LocalDate birthDate,
        String phone,
        String medicalHistory,
        String description
) {
}