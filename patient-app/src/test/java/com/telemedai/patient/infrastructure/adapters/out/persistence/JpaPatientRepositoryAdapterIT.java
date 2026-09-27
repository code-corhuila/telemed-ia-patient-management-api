package com.telemedai.patient.infrastructure.adapters.out.persistence;

import com.telemedai.patient.domain.model.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaPatientRepositoryAdapter.class, PatientMapper.class})
@Testcontainers
class JpaPatientRepositoryAdapterIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("telemed_patient_db")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Skip Flyway in tests — the test schema is created by Hibernate
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private JpaPatientRepositoryAdapter adapter;

    @Test
    @DisplayName("save: should persist a new patient and populate the generated id")
    void shouldSaveNewPatient() {
        Patient patient = Patient.createNew(1001L);
        patient.updateProfile(LocalDate.of(1990, 5, 15), "3001234567", null, null);

        Patient saved = adapter.save(patient);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(1001L);
        assertThat(saved.getPhone()).isEqualTo("3001234567");
    }

    @Test
    @DisplayName("findByUserId: should return the patient when it exists")
    void shouldFindByUserId() {
        Patient patient = Patient.createNew(1002L);
        adapter.save(patient);

        Optional<Patient> found = adapter.findByUserId(1002L);

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1002L);
    }

    @Test
    @DisplayName("findByUserId: should return empty when the patient does not exist")
    void shouldReturnEmptyWhenNotFound() {
        Optional<Patient> found = adapter.findByUserId(9999L);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("save: should update an existing patient")
    void shouldUpdateExistingPatient() {
        Patient patient = Patient.createNew(1003L);
        Patient saved = adapter.save(patient);

        saved.updateProfile(LocalDate.of(1985, 3, 20), "3000000000", "Diabetes", null);
        adapter.save(saved);

        Optional<Patient> reloaded = adapter.findByUserId(1003L);
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getPhone()).isEqualTo("3000000000");
        assertThat(reloaded.get().getMedicalHistory()).isEqualTo("Diabetes");
    }
}
