package com.telemedai.patient.application.ports.in;

import com.telemedai.patient.application.dto.PatientResponse;

/**
 * Input port for retrieving the patient profile associated with a given user.
 */
public interface GetPatientProfilePort {

    /**
     * Retrieves the patient profile for the given user identifier.
     *
     * @param userId the external reference to the authenticated user
     * @return the patient profile
     * @throws com.telemedai.patient.domain.exception.PatientNotFoundException
     *         if no patient is associated with the given userId
     */
    PatientResponse getByUserId(Long userId);
}
