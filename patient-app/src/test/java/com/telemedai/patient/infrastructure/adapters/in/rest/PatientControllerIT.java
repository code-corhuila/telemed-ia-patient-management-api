package com.telemedai.patient.infrastructure.adapters.in.rest;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.telemedai.patient.domain.model.Patient;
import com.telemedai.patient.infrastructure.adapters.out.persistence.JpaPatientRepositoryAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

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

    private static final KeyPair KEY_PAIR = generateKeyPair();

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            return gen.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String jwt(String sub, String role) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(sub)
                    .claim("role", role)
                    .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                    .issueTime(new Date())
                    .build();
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
            jwt.sign(new RSASSASigner((RSAPrivateKey) KEY_PAIR.getPrivate()));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        RSAPublicKey testJwtPublicKey() {
            return (RSAPublicKey) KEY_PAIR.getPublic();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        // Placeholder value — the actual bean is overridden by TestSecurityConfig
        registry.add("app.security.jwt.public-key", () -> "placeholder");
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
    @DisplayName("GET /api/patients/me: should return the authenticated patient's profile")
    void getMyProfile() throws Exception {
        mockMvc.perform(get("/api/patients/me")
                        .header("Authorization", "Bearer " + jwt("1001", "PATIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1001)))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/patients/me: should return 404 when the patient does not exist")
    void getMyProfileNotFound() throws Exception {
        mockMvc.perform(get("/api/patients/me")
                        .header("Authorization", "Bearer " + jwt("9999", "PATIENT")))
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
                        .header("Authorization", "Bearer " + jwt("1001", "PATIENT"))
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
                        .header("Authorization", "Bearer " + jwt("1001", "PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("GET /api/patients/me: should return 401 without a token")
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/patients/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("UNAUTHORIZED")));
    }
}
