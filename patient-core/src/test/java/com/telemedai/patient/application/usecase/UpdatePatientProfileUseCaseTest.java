package com.telemedai.patient.application.usecase;

import com.telemedai.patient.application.dto.PatientResponse;
import com.telemedai.patient.application.dto.UpdatePatientCommand;
import com.telemedai.patient.application.ports.out.PatientNotFoundException;
import com.telemedai.patient.application.ports.out.PatientRepositoryPort;
import com.telemedai.patient.domain.exception.InvalidBirthDateException;
import com.telemedai.patient.domain.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdatePatientProfileUseCaseTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000042");
    private static final UUID MISSING_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");

    private PatientRepositoryPort patientRepository;
    private UpdatePatientProfileUseCase useCase;

    @BeforeEach
    void setUp() {
        patientRepository = mock(PatientRepositoryPort.class);
        useCase = new UpdatePatientProfileUseCase(patientRepository);
    }

    @Test
    @DisplayName("update: should update the profile and return the updated data")
    void shouldUpdateProfile() {
        Patient patient = Patient.createNew(USER_ID);
        when(patientRepository.findByUserId(USER_ID)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdatePatientCommand command = new UpdatePatientCommand(
                USER_ID,
                LocalDate.of(1990, 5, 15),
                "3001234567",
                "Hypertension",
                "No known allergies"
        );

        PatientResponse response = useCase.update(command);

        assertThat(response.birthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(response.phone()).isEqualTo("3001234567");
        assertThat(response.medicalHistory()).isEqualTo("Hypertension");
    }

    @Test
    @DisplayName("update: should throw PatientNotFoundException when the patient does not exist")
    void shouldThrowWhenPatientNotFound() {
        when(patientRepository.findByUserId(MISSING_USER_ID)).thenReturn(Optional.empty());

        UpdatePatientCommand command = new UpdatePatientCommand(
                MISSING_USER_ID, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.update(command))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    @DisplayName("update: should propagate InvalidBirthDateException from the domain")
    void shouldPropagateInvalidBirthDate() {
        Patient patient = Patient.createNew(USER_ID);
        when(patientRepository.findByUserId(USER_ID)).thenReturn(Optional.of(patient));

        LocalDate futureDate = LocalDate.now().plusDays(1);
        UpdatePatientCommand command = new UpdatePatientCommand(
                USER_ID, futureDate, null, null, null
        );

        assertThatThrownBy(() -> useCase.update(command))
                .isInstanceOf(InvalidBirthDateException.class);
    }
}
