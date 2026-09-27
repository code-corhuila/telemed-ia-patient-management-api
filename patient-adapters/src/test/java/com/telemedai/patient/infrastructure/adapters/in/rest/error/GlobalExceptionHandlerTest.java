package com.telemedai.patient.infrastructure.adapters.in.rest.error;

import com.telemedai.patient.application.ports.out.PatientNotFoundException;
import com.telemedai.patient.domain.exception.DomainException;
import com.telemedai.patient.domain.exception.InvalidBirthDateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void patientNotFound_maps_to_404() {
        MDC.put(CorrelationFilter.MDC_KEY, "corr-1");
        ResponseEntity<ApiError> r = handler.handlePatientNotFound(new PatientNotFoundException(1L));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(r.getBody().error()).isEqualTo("NOT_FOUND");
        assertThat(r.getBody().traceId()).isEqualTo("corr-1");
    }

    @Test
    void invalidBirthDate_maps_to_422() {
        MDC.put(CorrelationFilter.MDC_KEY, "corr-2");
        ResponseEntity<ApiError> r = handler.handleInvalidBirthDate(new InvalidBirthDateException("future"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(r.getBody().error()).isEqualTo("BUSINESS_RULE_VIOLATION");
    }

    @Test
    void genericException_maps_to_500_with_neutral_message() {
        MDC.put(CorrelationFilter.MDC_KEY, "corr-3");
        ResponseEntity<ApiError> r = handler.handleAny(new RuntimeException("internal db details leaked here"),
                new org.springframework.mock.web.MockHttpServletRequest());
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(r.getBody().error()).isEqualTo("INTERNAL_ERROR");
        assertThat(r.getBody().message()).isEqualTo("an unexpected error occurred");
        assertThat(r.getBody().message()).doesNotContain("db details");
    }

    @Test
    void missing_correlation_id_uses_unknown() {
        ResponseEntity<ApiError> r = handler.handlePatientNotFound(new PatientNotFoundException(1L));
        assertThat(r.getBody().traceId()).isEqualTo("unknown");
    }
}