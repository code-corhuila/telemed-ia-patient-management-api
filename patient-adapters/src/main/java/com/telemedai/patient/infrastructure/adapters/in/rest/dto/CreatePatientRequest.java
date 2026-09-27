package com.telemedai.patient.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePatientRequest(
        LocalDate birthDate,
        @Size(max = 30) String phone,
        String medicalHistory,
        String description
) {
}