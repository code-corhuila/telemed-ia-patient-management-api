package com.telemedai.patient.infrastructure.config;

import com.telemedai.patient.application.ports.out.PatientRepositoryPort;
import com.telemedai.patient.application.usecase.GetPatientProfileUseCase;
import com.telemedai.patient.application.usecase.UpdatePatientProfileUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that wires the application layer use cases as beans.
 *
 * The use cases themselves are plain classes (no Spring annotations). This
 * configuration is the only place where they become managed beans, keeping
 * the application layer framework-agnostic.
 */
@Configuration
public class ApplicationConfiguration {

    @Bean
    public GetPatientProfileUseCase getPatientProfileUseCase(
            PatientRepositoryPort patientRepositoryPort) {
        return new GetPatientProfileUseCase(patientRepositoryPort);
    }

    @Bean
    public UpdatePatientProfileUseCase updatePatientProfileUseCase(
            PatientRepositoryPort patientRepositoryPort) {
        return new UpdatePatientProfileUseCase(patientRepositoryPort);
    }
}
