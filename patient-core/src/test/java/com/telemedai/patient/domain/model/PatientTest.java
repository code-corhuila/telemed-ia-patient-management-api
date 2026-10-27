package com.telemedai.patient.domain.model;

import com.telemedai.patient.domain.exception.InvalidBirthDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatientTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000042");
    private static final UUID PATIENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    @DisplayName("createNew: should create a patient with the given userId and null optional fields")
    void shouldCreateNewPatient() {
        Patient patient = Patient.createNew(USER_ID);

        assertThat(patient.getId()).isNull();
        assertThat(patient.getUserId()).isEqualTo(USER_ID);
        assertThat(patient.getBirthDate()).isNull();
        assertThat(patient.getPhone()).isNull();
        assertThat(patient.getMedicalHistory()).isNull();
        assertThat(patient.getDescription()).isNull();
        assertThat(patient.getCreatedAt()).isNotNull();
        assertThat(patient.getUpdatedAt()).isNotNull();
        assertThat(patient.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("createNew: should reject a null userId")
    void shouldRejectNullUserIdOnCreate() {
        assertThatThrownBy(() -> Patient.createNew(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("updateProfile: should update all editable fields")
    void shouldUpdateProfile() {
        Patient patient = Patient.createNew(USER_ID);
        OffsetDateTime beforeUpdate = patient.getUpdatedAt();

        patient.updateProfile(
                LocalDate.of(1990, 5, 15),
                "3001234567",
                "Hypertension",
                "No known allergies"
        );

        assertThat(patient.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(patient.getPhone()).isEqualTo("3001234567");
        assertThat(patient.getMedicalHistory()).isEqualTo("Hypertension");
        assertThat(patient.getDescription()).isEqualTo("No known allergies");
        assertThat(patient.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    @DisplayName("updateProfile: should reject a future birth date")
    void shouldRejectFutureBirthDate() {
        Patient patient = Patient.createNew(USER_ID);
        LocalDate futureDate = LocalDate.now().plusDays(1);

        assertThatThrownBy(() ->
                patient.updateProfile(futureDate, null, null, null)
        ).isInstanceOf(InvalidBirthDateException.class)
         .hasMessageContaining("future");
    }

    @Test
    @DisplayName("updateProfile: should accept a null birth date")
    void shouldAcceptNullBirthDate() {
        Patient patient = Patient.createNew(USER_ID);

        patient.updateProfile(null, "3001234567", null, null);

        assertThat(patient.getBirthDate()).isNull();
        assertThat(patient.getPhone()).isEqualTo("3001234567");
    }

    @Test
    @DisplayName("softDelete: should set deletedAt and updatedAt")
    void shouldSoftDelete() {
        Patient patient = Patient.createNew(USER_ID);

        patient.softDelete();

        assertThat(patient.getDeletedAt()).isNotNull();
        assertThat(patient.getUpdatedAt()).isEqualTo(patient.getDeletedAt());
    }

    @Test
    @DisplayName("softDelete: should be idempotent")
    void softDeleteShouldBeIdempotent() {
        Patient patient = Patient.createNew(USER_ID);

        patient.softDelete();
        OffsetDateTime firstDeletedAt = patient.getDeletedAt();

        patient.softDelete();

        assertThat(patient.getDeletedAt()).isEqualTo(firstDeletedAt);
    }

    @Test
    @DisplayName("reconstitute: should rebuild the aggregate from persisted values")
    void shouldReconstitutePatient() {
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(10);
        OffsetDateTime updatedAt = OffsetDateTime.now().minusDays(2);

        Patient patient = Patient.reconstitute(
                PATIENT_ID,
                USER_ID,
                LocalDate.of(1990, 5, 15),
                "3001234567",
                "Hypertension",
                "No known allergies",
                createdAt,
                updatedAt,
                null
        );

        assertThat(patient.getId()).isEqualTo(PATIENT_ID);
        assertThat(patient.getUserId()).isEqualTo(USER_ID);
        assertThat(patient.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(patient.getCreatedAt()).isEqualTo(createdAt);
        assertThat(patient.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
