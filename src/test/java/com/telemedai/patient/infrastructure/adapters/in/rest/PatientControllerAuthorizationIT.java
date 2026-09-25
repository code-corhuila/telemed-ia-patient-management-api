package com.telemedai.patient.infrastructure.adapters.in.rest;

import com.telemedai.patient.domain.model.Patient;
import com.telemedai.patient.infrastructure.adapters.out.persistence.JpaPatientRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PatientControllerAuthorizationIT {

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
        adapter.save(Patient.createNew(1001L));
    }

    @Test
    @DisplayName("GET /me: should return 403 when the role is ADMIN")
    void shouldForbidAdminRole() throws Exception {
        mockMvc.perform(get("/api/patients/me")
                        .header("X-Gateway-Secret", "local-dev-secret")
                        .header("X-User-Id", 1001L)
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /me: should return 403 when the role is PROFESSIONAL")
    void shouldForbidProfessionalRole() throws Exception {
        mockMvc.perform(get("/api/patients/me")
                        .header("X-Gateway-Secret", "local-dev-secret")
                        .header("X-User-Id", 1001L)
                        .header("X-User-Role", "PROFESSIONAL"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /me: should allow the PATIENT role")
    void shouldAllowPatientRole() throws Exception {
        mockMvc.perform(get("/api/patients/me")
                        .header("X-Gateway-Secret", "local-dev-secret")
                        .header("X-User-Id", 1001L)
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk());
    }
}
