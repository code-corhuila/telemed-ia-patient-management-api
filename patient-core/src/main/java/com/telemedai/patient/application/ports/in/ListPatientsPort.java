package com.telemedai.patient.application.ports.in;

import com.telemedai.patient.application.dto.PageResponse;
import com.telemedai.patient.application.dto.PatientResponse;

public interface ListPatientsPort {
    PageResponse<PatientResponse> list(int page, int limit);
}