package com.telemedai.patient.infrastructure.adapters.in.rest;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.telemedai.patient.domain.model.Patient;
import com.telemedai.patient.infrastructure.adapters.out.persistence.JpaPatientRepositoryAdapter;
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
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

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

    // ---------- GET /me ----------

    @Test
    @DisplayName("GET /me: should return 403 when the role is ADMIN")
    void shouldForbidAdminRole() throws Exception {
        mockMvc.perform(get("/api/patients/me")
                        .header("Authorization", "Bearer " + jwt("1001", "ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /me: should return 403 when the role is PROFESSIONAL")
    void shouldForbidProfessionalRole() throws Exception {
        mockMvc.perform(get("/api/patients/me")
                        .header("Authorization", "Bearer " + jwt("1001", "PROFESSIONAL")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /me: should allow the PATIENT role")
    void shouldAllowPatientRole() throws Exception {
        mockMvc.perform(get("/api/patients/me")
                        .header("Authorization", "Bearer " + jwt("1001", "PATIENT")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /me: should return 401 without a token")
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/patients/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("UNAUTHORIZED")));
    }

    @Test
    @DisplayName("GET /me: should return 401 with a malformed token")
    void shouldReturn401WithMalformedToken() throws Exception {
        mockMvc.perform(get("/api/patients/me")
                        .header("Authorization", "Bearer not-a-real-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("UNAUTHORIZED")));
    }

    // ---------- PUT /me ----------

    @Test
    @DisplayName("PUT /me: should return 403 when the role is ADMIN")
    void shouldForbidAdminRoleOnPut() throws Exception {
        String body = """
                {
                    "birthDate": "1990-05-15",
                    "phone": "3001234567"
                }
                """;

        mockMvc.perform(put("/api/patients/me")
                        .header("Authorization", "Bearer " + jwt("1001", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /me: should return 403 when the role is PROFESSIONAL")
    void shouldForbidProfessionalRoleOnPut() throws Exception {
        String body = """
                {
                    "birthDate": "1990-05-15",
                    "phone": "3001234567"
                }
                """;

        mockMvc.perform(put("/api/patients/me")
                        .header("Authorization", "Bearer " + jwt("1001", "PROFESSIONAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /me: should allow the PATIENT role")
    void shouldAllowPatientRoleOnPut() throws Exception {
        String body = """
                {
                    "birthDate": "1990-05-15",
                    "phone": "3001234567"
                }
                """;

        mockMvc.perform(put("/api/patients/me")
                        .header("Authorization", "Bearer " + jwt("1001", "PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}