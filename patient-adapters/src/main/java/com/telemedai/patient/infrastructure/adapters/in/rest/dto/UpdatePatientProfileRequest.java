package com.telemedai.patient.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * REST request body for updating a patient profile.
 *
 * The `userId` is intentionally absent: it is extracted from the authenticated
 * identity (the `X-User-Id` header set by the API Gateway), never from the
 * request body.
 */
public record UpdatePatientProfileRequest(

        @Past(message = "birthDate must be a past date")
        LocalDate birthDate,

        @Size(max = 30, message = "phone must not exceed 30 characters")
        String phone,

        String medicalHistory,

        String description
) {
}
