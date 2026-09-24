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
    @DisplayName("getByUserId: should return the patient profile when found")
    void shouldReturnPatientProfile() {
        Patient patient = Patient.createNew(42L);
        when(patientRepository.findByUserId(42L)).thenReturn(Optional.of(patient));

        PatientResponse response = useCase.getByUserId(42L);

        assertThat(response.userId()).isEqualTo(42L);
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
