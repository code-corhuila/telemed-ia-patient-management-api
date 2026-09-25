package com.telemedai.patient.infrastructure.adapters.in.rest;

import com.telemedai.patient.domain.model.Patient;
import com.telemedai.patient.infrastructure.adapters.out.persistence.JpaPatientRepositoryAdapter;
import com.telemedai.patient.infrastructure.adapters.out.persistence.PatientMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PatientControllerIT {

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
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaPatientRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        // Seed a patient for user 1001
        Patient patient = Patient.createNew(1001L);
        adapter.save(patient);
    }

    @Test
    @DisplayName("GET /api/patients/me: should return the authenticated patient's profile")
    void getMyProfile() throws Exception {
        mockMvc.perform(get("/api/patients/me")
                        .header("X-User-Id", 1001L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1001)))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/patients/me: should return 404 when the patient does not exist")
    void getMyProfileNotFound() throws Exception {
        mockMvc.perform(get("/api/patients/me")
                        .header("X-User-Id", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
    }

    @Test
    @DisplayName("PUT /api/patients/me: should update the profile")
    void updateMyProfile() throws Exception {
        String body = """
                {
                    "birthDate": "1990-05-15",
                    "phone": "3001234567",
                    "medicalHistory": "Hypertension",
                    "description": "No known allergies"
                }
                """;

        mockMvc.perform(put("/api/patients/me")
                        .header("X-User-Id", 1001L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone", is("3001234567")))
                .andExpect(jsonPath("$.medicalHistory", is("Hypertension")));
    }

    @Test
    @DisplayName("PUT /api/patients/me: should reject a future birth date")
    void updateWithFutureBirthDate() throws Exception {
        String futureDate = LocalDate.now().plusDays(1).toString();
        String body = """
                {
                    "birthDate": "%s"
                }
                """.formatted(futureDate);

        mockMvc.perform(put("/api/patients/me")
                        .header("X-User-Id", 1001L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }
}
