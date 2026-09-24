package com.telemedai.patient.application.usecase;

import com.telemedai.patient.application.dto.PatientResponse;
import com.telemedai.patient.application.ports.out.PatientRepositoryPort;
import com.telemedai.patient.domain.exception.PatientNotFoundException;
import com.telemedai.patient.domain.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetPatientProfileUseCaseTest {

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
                1L,
                42L,
                LocalDate.of(1990, 5, 15),
                "3001234567",
                "Hypertension",
                "No known allergies",
                OffsetDateTime.now().minusDays(10),
                OffsetDateTime.now().minusDays(2),
                null
        );
        when(patientRepository.findByUserId(42L)).thenReturn(Optional.of(patient));

        PatientResponse response = useCase.getByUserId(42L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo(42L);
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
        when(patientRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getByUserId(99L))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining("99");
    }
}
