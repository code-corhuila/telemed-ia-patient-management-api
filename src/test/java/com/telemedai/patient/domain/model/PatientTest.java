package com.telemedai.patient.domain.model;

import com.telemedai.patient.domain.exception.InvalidBirthDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the Patient aggregate.
 * These tests do not require Spring, a database, or any infrastructure.
 */
class PatientTest {

    @Test
    @DisplayName("createNew: should create a patient with the given userId and null optional fields")
    void shouldCreateNewPatient() {
        Patient patient = Patient.createNew(42L);

        assertThat(patient.getId()).isNull();
        assertThat(patient.getUserId()).isEqualTo(42L);
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
        Patient patient = Patient.createNew(42L);
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
        Patient patient = Patient.createNew(42L);
        LocalDate futureDate = LocalDate.now().plusDays(1);

        assertThatThrownBy(() ->
                patient.updateProfile(futureDate, null, null, null)
        ).isInstanceOf(InvalidBirthDateException.class)
         .hasMessageContaining("future");
    }

    @Test
    @DisplayName("updateProfile: should accept a null birth date")
    void shouldAcceptNullBirthDate() {
        Patient patient = Patient.createNew(42L);

        patient.updateProfile(null, "3001234567", null, null);

        assertThat(patient.getBirthDate()).isNull();
        assertThat(patient.getPhone()).isEqualTo("3001234567");
    }

    @Test
    @DisplayName("softDelete: should set deletedAt and updatedAt")
    void shouldSoftDelete() {
        Patient patient = Patient.createNew(42L);

        patient.softDelete();

        assertThat(patient.getDeletedAt()).isNotNull();
        assertThat(patient.getUpdatedAt()).isEqualTo(patient.getDeletedAt());
    }

        @Test
    @DisplayName("softDelete: should be idempotent")
    void softDeleteShouldBeIdempotent() {
        Patient patient = Patient.createNew(42L);

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
                1L,
                42L,
                LocalDate.of(1990, 5, 15),
                "3001234567",
                "Hypertension",
                "No known allergies",
                createdAt,
                updatedAt,
                null
        );

        assertThat(patient.getId()).isEqualTo(1L);
        assertThat(patient.getUserId()).isEqualTo(42L);
        assertThat(patient.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(patient.getCreatedAt()).isEqualTo(createdAt);
        assertThat(patient.getUpdatedAt()).isEqualTo(updatedAt);
    }
}