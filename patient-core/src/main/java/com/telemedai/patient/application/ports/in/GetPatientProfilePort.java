package com.telemedai.patient.application.ports.in;

import com.telemedai.patient.application.dto.PatientResponse;

import java.util.UUID;

public interface GetPatientProfilePort {

    PatientResponse getByUserId(UUID userId);
}
