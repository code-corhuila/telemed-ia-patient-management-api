package com.telemedai.patient.application.ports.in;

import com.telemedai.patient.application.dto.PatientResponse;
import com.telemedai.patient.application.dto.UpdatePatientCommand;

/**
 * Input port for updating the patient profile.
 */
public interface UpdatePatientProfilePort {

    /**
     * Updates the patient profile for the user identified in the command.
     *
     * @param command the new profile values
     * @return the updated patient profile
     * @throws com.telemedai.patient.domain.exception.PatientNotFoundException
     *         if no patient is associated with the given userId
     */
    PatientResponse update(UpdatePatientCommand command);
}
