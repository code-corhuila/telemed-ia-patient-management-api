package com.telemedai.patient.application.usecase;

import com.telemedai.patient.application.dto.PageResponse;
import com.telemedai.patient.application.dto.PatientResponse;
import com.telemedai.patient.application.ports.in.ListPatientsPort;
import com.telemedai.patient.application.ports.out.PatientRepositoryPort;
import com.telemedai.patient.domain.exception.InvalidPaginationException;
import com.telemedai.patient.domain.model.Patient;

import java.util.List;

public class ListPatientsUseCase implements ListPatientsPort {

    private static final int MAX_LIMIT = 100;
    private static final int DEFAULT_LIMIT = 20;

    private final PatientRepositoryPort patientRepository;

    public ListPatientsUseCase(PatientRepositoryPort patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public PageResponse<PatientResponse> list(int page, int limit) {
        if (page < 1) {
            throw new InvalidPaginationException("page must be >= 1");
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new InvalidPaginationException("limit must be between 1 and " + MAX_LIMIT);
        }

        int offset = (page - 1) * limit;
        List<Patient> patients = patientRepository.findAllActive(offset, limit);
        long total = patientRepository.countActive();

        List<PatientResponse> data = patients.stream()
                .map(PatientResponse::from)
                .toList();

        return new PageResponse<>(data, PageResponse.Meta.of(page, limit, total));
    }

    public static int defaultLimit() {
        return DEFAULT_LIMIT;
    }
}