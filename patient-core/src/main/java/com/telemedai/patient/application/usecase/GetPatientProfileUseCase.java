package com.telemedai.patient.application.usecase;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import com.telemedai.patient.application.dto.PatientResponse;
import com.telemedai.patient.application.ports.in.GetPatientProfilePort;
import com.telemedai.patient.application.ports.out.PatientRepositoryPort;
import com.telemedai.patient.application.ports.out.PatientNotFoundException;
import com.telemedai.patient.domain.model.Patient;

/**
 * Retrieves the patient profile for a given user.
 *
 * The use case is intentionally thin: it loads the aggregate through the
 * repository port and maps it to a DTO. Authorization (that the caller is
 * allowed to see this profile) is enforced at the REST layer, where the
 * authenticated user identity is available.
 */
public class GetPatientProfileUseCase implements GetPatientProfilePort {

    private final PatientRepositoryPort patientRepository;

    public GetPatientProfileUseCase(PatientRepositoryPort patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public PatientResponse getByUserId(Long userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new PatientNotFoundException(userId));
        return PatientResponse.from(patient);
    }
}
