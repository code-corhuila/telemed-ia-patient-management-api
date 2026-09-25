package com.telemedai.patient.infrastructure.adapters.in.rest;

import com.telemedai.patient.application.dto.PatientResponse;
import com.telemedai.patient.application.dto.UpdatePatientCommand;
import com.telemedai.patient.application.ports.in.GetPatientProfilePort;
import com.telemedai.patient.application.ports.in.UpdatePatientProfilePort;
import com.telemedai.patient.infrastructure.adapters.in.rest.dto.UpdatePatientProfileRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter that exposes the patient profile endpoints.
 *
 * The authenticated user identity is read from the `X-User-Id` header, which
 * is set by the API Gateway after validating the JWT. In this PR the header
 * is trusted; a follow-up PR adds formal Spring Security integration.
 *
 * Endpoints (see 07-api/contracts/openapi/patient-service.yaml):
 *   GET  /api/patients/me   — view own profile
 *   PUT  /api/patients/me   — update own profile
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final GetPatientProfilePort getPatientProfileUseCase;
    private final UpdatePatientProfilePort updatePatientProfileUseCase;

    public PatientController(
            GetPatientProfilePort getPatientProfileUseCase,
            UpdatePatientProfilePort updatePatientProfileUseCase
    ) {
        this.getPatientProfileUseCase = getPatientProfileUseCase;
        this.updatePatientProfileUseCase = updatePatientProfileUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<PatientResponse> getMyProfile(
            @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(getPatientProfileUseCase.getByUserId(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<PatientResponse> updateMyProfile(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdatePatientProfileRequest request
    ) {
        UpdatePatientCommand command = new UpdatePatientCommand(
                userId,
                request.birthDate(),
                request.phone(),
                request.medicalHistory(),
                request.description()
        );
        return ResponseEntity.ok(updatePatientProfileUseCase.update(command));
    }
}
