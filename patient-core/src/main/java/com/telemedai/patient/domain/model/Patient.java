package com.telemedai.patient.domain.model;

import com.telemedai.patient.domain.exception.InvalidBirthDateException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Patient aggregate root.
 *
 * Owns the patient profile lifecycle: creation, profile update, and soft delete.
 * Contains only business rules and does not depend on Spring, JPA, or any other
 * infrastructure technology.
 *
 * Identifiers are UUIDs:
 *   - `id`     internal primary key of the patient record
 *   - `userId` external reference to the Identity `sub` (JWT subject)
 */
public class Patient {

    private UUID id;
    private final UUID userId;
    private LocalDate birthDate;
    private String phone;
    private String medicalHistory;
    private String description;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;

    private Patient(
            UUID id,
            UUID userId,
            LocalDate birthDate,
            String phone,
            String medicalHistory,
            String description,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime deletedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.birthDate = birthDate;
        this.phone = phone;
        this.medicalHistory = medicalHistory;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    /**
     * Creates a new patient profile for a given user.
     * The profile starts with empty optional fields.
     */
    public static Patient createNew(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        OffsetDateTime now = OffsetDateTime.now();
        return new Patient(null, userId, null, null, null, null, now, now, null);
    }

    /**
     * Reconstitutes a patient from persisted state.
     * Used by persistence adapters when loading from the database.
     */
    public static Patient reconstitute(
            UUID id,
            UUID userId,
            LocalDate birthDate,
            String phone,
            String medicalHistory,
            String description,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime deletedAt
    ) {
        return new Patient(id, userId, birthDate, phone, medicalHistory, description,
                createdAt, updatedAt, deletedAt);
    }

    /**
     * Updates the editable profile fields.
     * Validates the birth date invariant.
     */
    public void updateProfile(LocalDate birthDate, String phone,
                              String medicalHistory, String description) {
        validateBirthDate(birthDate);
        this.birthDate = birthDate;
        this.phone = phone;
        this.medicalHistory = medicalHistory;
        this.description = description;
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Marks the patient as soft-deleted.
     * The record remains in the database with a `deletedAt` timestamp.
     *
     * Idempotent: calling this method on an already-deleted patient is a no-op.
     * This preserves the original deletion timestamp if a retry or a duplicate
     * request reaches the aggregate.
     */
    public void softDelete() {
        if (this.deletedAt != null) {
            return;
        }
        this.deletedAt = OffsetDateTime.now();
        this.updatedAt = this.deletedAt;
    }

    private static void validateBirthDate(LocalDate birthDate) {
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new InvalidBirthDateException("Birth date must not be in the future");
        }
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }
}
