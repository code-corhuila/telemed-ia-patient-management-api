package com.telemedai.patient.infrastructure.adapters.in.rest;

import com.telemedai.patient.application.dto.PatientResponse;
import com.telemedai.patient.application.dto.UpdatePatientCommand;
import com.telemedai.patient.application.ports.in.GetPatientProfilePort;
import com.telemedai.patient.application.ports.in.UpdatePatientProfilePort;
import com.telemedai.patient.infrastructure.adapters.in.rest.dto.UpdatePatientProfileRequest;
import com.telemedai.patient.infrastructure.config.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter that exposes the patient profile endpoints.
 *
 * The authenticated identity comes from `@AuthenticationPrincipal`, which is
 * populated by the security filter chain after validating the gateway trust
 * headers.
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
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponse> getMyProfile(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(getPatientProfileUseCase.getByUserId(user.userId()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponse> updateMyProfile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UpdatePatientProfileRequest request
    ) {
        UpdatePatientCommand command = new UpdatePatientCommand(
                user.userId(),
                request.birthDate(),
                request.phone(),
                request.medicalHistory(),
                request.description()
        );
        return ResponseEntity.ok(updatePatientProfileUseCase.update(command));
    }
}
