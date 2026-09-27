package com.telemedai.patient.application.usecase;

import com.telemedai.patient.application.dto.CreatePatientCommand;
import com.telemedai.patient.application.dto.PatientResponse;
import com.telemedai.patient.application.ports.in.CreatePatientPort;
import com.telemedai.patient.application.ports.out.IdempotencyKeyRepositoryPort;
import com.telemedai.patient.application.ports.out.PatientRepositoryPort;
import com.telemedai.patient.domain.exception.InvalidIdempotencyKeyException;
import com.telemedai.patient.domain.model.IdempotencyRecord;
import com.telemedai.patient.domain.model.Patient;

import java.time.OffsetDateTime;
import java.util.Optional;

public class CreatePatientUseCase implements CreatePatientPort {

    private static final int MIN_KEY_LENGTH = 8;
    private static final int MAX_KEY_LENGTH = 128;

    private final PatientRepositoryPort patientRepository;
    private final IdempotencyKeyRepositoryPort idempotencyRepository;

    public CreatePatientUseCase(PatientRepositoryPort patientRepository,
                                IdempotencyKeyRepositoryPort idempotencyRepository) {
        this.patientRepository = patientRepository;
        this.idempotencyRepository = idempotencyRepository;
    }

    @Override
    public PatientResponse create(CreatePatientCommand command, String idempotencyKey) {
        validateKey(idempotencyKey);

        Optional<IdempotencyRecord> existing = idempotencyRepository.findByKey(idempotencyKey);
        if (existing.isPresent()) {
            Patient existingPatient = patientRepository.findById(existing.get().resourceId())
                    .orElseThrow(() -> new IllegalStateException(
                            "idempotency key points to a missing resource"));
            return PatientResponse.from(existingPatient);
        }

        Patient patient = Patient.createNew(command.userId());
        patient.updateProfile(
                command.birthDate(),
                command.phone(),
                command.medicalHistory(),
                command.description()
        );
        Patient saved = patientRepository.save(patient);

        idempotencyRepository.save(new IdempotencyRecord(
                idempotencyKey,
                command.userId(),
                saved.getId(),
                OffsetDateTime.now()
        ));

        return PatientResponse.from(saved);
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new InvalidIdempotencyKeyException("Idempotency-Key header is required");
        }
        if (key.length() < MIN_KEY_LENGTH || key.length() > MAX_KEY_LENGTH) {
            throw new InvalidIdempotencyKeyException(
                    "Idempotency-Key must be between " + MIN_KEY_LENGTH + " and "
                            + MAX_KEY_LENGTH + " characters");
        }
    }
}