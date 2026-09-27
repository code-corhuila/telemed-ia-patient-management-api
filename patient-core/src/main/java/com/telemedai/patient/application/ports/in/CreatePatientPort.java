package com.telemedai.patient.application.ports.in;

import com.telemedai.patient.application.dto.CreatePatientCommand;
import com.telemedai.patient.application.dto.PatientResponse;

public interface CreatePatientPort {
    PatientResponse create(CreatePatientCommand command, String idempotencyKey);
}