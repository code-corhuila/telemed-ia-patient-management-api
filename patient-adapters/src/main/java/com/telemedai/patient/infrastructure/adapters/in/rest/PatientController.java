package com.telemedai.patient.infrastructure.adapters.in.rest;

import com.telemedai.patient.application.dto.CreatePatientCommand;
import com.telemedai.patient.application.dto.PageResponse;
import com.telemedai.patient.application.dto.PatientResponse;
import com.telemedai.patient.application.dto.UpdatePatientCommand;
import com.telemedai.patient.application.ports.in.CreatePatientPort;
import com.telemedai.patient.application.ports.in.GetPatientProfilePort;
import com.telemedai.patient.application.ports.in.ListPatientsPort;
import com.telemedai.patient.application.ports.in.UpdatePatientProfilePort;
import com.telemedai.patient.application.usecase.ListPatientsUseCase;
import com.telemedai.patient.infrastructure.adapters.in.rest.dto.CreatePatientRequest;
import com.telemedai.patient.infrastructure.adapters.in.rest.dto.UpdatePatientProfileRequest;
import com.telemedai.patient.infrastructure.config.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final GetPatientProfilePort getPatientProfileUseCase;
    private final UpdatePatientProfilePort updatePatientProfileUseCase;
    private final CreatePatientPort createPatientUseCase;
    private final ListPatientsPort listPatientsUseCase;

    public PatientController(
            GetPatientProfilePort getPatientProfileUseCase,
            UpdatePatientProfilePort updatePatientProfileUseCase,
            CreatePatientPort createPatientUseCase,
            ListPatientsPort listPatientsUseCase
    ) {
        this.getPatientProfileUseCase = getPatientProfileUseCase;
        this.updatePatientProfileUseCase = updatePatientProfileUseCase;
        this.createPatientUseCase = createPatientUseCase;
        this.listPatientsUseCase = listPatientsUseCase;
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

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponse> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreatePatientRequest request
    ) {
        CreatePatientCommand command = new CreatePatientCommand(
                user.userId(),
                request.birthDate(),
                request.phone(),
                request.medicalHistory(),
                request.description()
        );
        PatientResponse response = createPatientUseCase.create(command, idempotencyKey);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT','PROFESSIONAL','ADMIN')")
    public ResponseEntity<PageResponse<PatientResponse>> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        int effectiveLimit = limit != null ? limit : ListPatientsUseCase.defaultLimit();
        return ResponseEntity.ok(listPatientsUseCase.list(page, effectiveLimit));
    }
}