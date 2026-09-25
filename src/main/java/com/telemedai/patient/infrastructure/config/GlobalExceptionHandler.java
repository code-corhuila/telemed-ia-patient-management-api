package com.telemedai.patient.infrastructure.config;

import com.telemedai.patient.domain.exception.DomainException;
import com.telemedai.patient.domain.exception.InvalidBirthDateException;
import com.telemedai.patient.domain.exception.PatientNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Translates domain and validation exceptions into the standard HTTP error
 * response used across the project.
 *
 * Error format (see 07-api/contracts/openapi/_shared.yaml):
 * {
 *   "error": "...",
 *   "message": "...",
 *   "details": [...],
 *   "traceId": "..."
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePatientNotFound(PatientNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidBirthDateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidBirthDate(InvalidBirthDateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), null);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, Object>> handleDomainException(DomainException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "DOMAIN_ERROR", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> Map.of(
                        "field", err.getField(),
                        "message", err.getDefaultMessage() != null ? err.getDefaultMessage() : ""
                ))
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "One or more fields are invalid", details);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String errorCode,
            String message,
            List<Map<String, String>> details
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", errorCode);
        body.put("message", message);
        if (details != null) {
            body.put("details", details);
        }
        body.put("traceId", "n/a"); // Placeholder until a correlationId filter is added
        return ResponseEntity.status(status).body(body);
    }
}
