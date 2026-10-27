package com.telemedai.patient.application.usecase;

import com.telemedai.patient.application.dto.PatientResponse;
import com.telemedai.patient.application.ports.in.GetPatientProfilePort;
import com.telemedai.patient.application.ports.out.PatientNotFoundException;
import com.telemedai.patient.application.ports.out.PatientRepositoryPort;
import com.telemedai.patient.domain.model.Patient;

import java.util.UUID;

public class GetPatientProfileUseCase implements GetPatientProfilePort {

    private final PatientRepositoryPort patientRepository;

    public GetPatientProfileUseCase(PatientRepositoryPort patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public PatientResponse getByUserId(UUID userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new PatientNotFoundException(userId));
        return PatientResponse.from(patient);
    }
}
