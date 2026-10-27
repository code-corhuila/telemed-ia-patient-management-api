package com.telemedai.patient.application.usecase;

import com.telemedai.patient.application.dto.PatientResponse;
import com.telemedai.patient.application.ports.out.PatientNotFoundException;
import com.telemedai.patient.application.ports.out.PatientRepositoryPort;
import com.telemedai.patient.domain.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetPatientProfileUseCaseTest {

    private static final UUID PATIENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000042");
    private static final UUID MISSING_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");

    private PatientRepositoryPort patientRepository;
    private GetPatientProfileUseCase useCase;

    @BeforeEach
    void setUp() {
        patientRepository = mock(PatientRepositoryPort.class);
        useCase = new GetPatientProfileUseCase(patientRepository);
    }

    @Test
    @DisplayName("getByUserId: should map all fields from the domain aggregate")
    void shouldReturnPatientProfileWithAllFieldsMapped() {
        Patient patient = Patient.reconstitute(
                PATIENT_ID,
                USER_ID,
                LocalDate.of(1990, 5, 15),
                "3001234567",
                "Hypertension",
                "No known allergies",
                OffsetDateTime.now().minusDays(10),
                OffsetDateTime.now().minusDays(2),
                null
        );
        when(patientRepository.findByUserId(USER_ID)).thenReturn(Optional.of(patient));

        PatientResponse response = useCase.getByUserId(USER_ID);

        assertThat(response.id()).isEqualTo(PATIENT_ID);
        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.birthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(response.phone()).isEqualTo("3001234567");
        assertThat(response.medicalHistory()).isEqualTo("Hypertension");
        assertThat(response.description()).isEqualTo("No known allergies");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("getByUserId: should throw PatientNotFoundException when not found")
    void shouldThrowWhenNotFound() {
        when(patientRepository.findByUserId(MISSING_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getByUserId(MISSING_USER_ID))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining(MISSING_USER_ID.toString());
    }
}
