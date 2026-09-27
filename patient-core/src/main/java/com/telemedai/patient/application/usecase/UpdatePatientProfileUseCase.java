package com.telemedai.patient.application.usecase;

import com.telemedai.patient.application.dto.PatientResponse;
import com.telemedai.patient.application.dto.UpdatePatientCommand;
import com.telemedai.patient.application.ports.in.UpdatePatientProfilePort;
import com.telemedai.patient.application.ports.out.PatientRepositoryPort;
import com.telemedai.patient.application.ports.out.PatientNotFoundException;
import com.telemedai.patient.domain.model.Patient;

/**
 * Updates the patient profile for a given user.
 *
 * The use case loads the aggregate, delegates the mutation to the domain
 * (which enforces the invariants), persists the result, and returns a DTO.
 */
public class UpdatePatientProfileUseCase implements UpdatePatientProfilePort {

    private final PatientRepositoryPort patientRepository;

    public UpdatePatientProfileUseCase(PatientRepositoryPort patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public PatientResponse update(UpdatePatientCommand command) {
        Patient patient = patientRepository.findByUserId(command.userId())
                .orElseThrow(() -> new PatientNotFoundException(command.userId()));

        patient.updateProfile(
                command.birthDate(),
                command.phone(),
                command.medicalHistory(),
                command.description()
        );

        Patient saved = patientRepository.save(patient);
        return PatientResponse.from(saved);
    }
}
