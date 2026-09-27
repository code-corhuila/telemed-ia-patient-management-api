package com.telemedai.patient.infrastructure.config;

import com.telemedai.patient.application.ports.in.CreatePatientPort;
import com.telemedai.patient.application.ports.in.GetPatientProfilePort;
import com.telemedai.patient.application.ports.in.ListPatientsPort;
import com.telemedai.patient.application.ports.in.UpdatePatientProfilePort;
import com.telemedai.patient.application.ports.out.IdempotencyKeyRepositoryPort;
import com.telemedai.patient.application.ports.out.PatientRepositoryPort;
import com.telemedai.patient.application.usecase.CreatePatientUseCase;
import com.telemedai.patient.application.usecase.GetPatientProfileUseCase;
import com.telemedai.patient.application.usecase.ListPatientsUseCase;
import com.telemedai.patient.application.usecase.UpdatePatientProfileUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public GetPatientProfilePort getPatientProfileUseCase(PatientRepositoryPort repository) {
        return new GetPatientProfileUseCase(repository);
    }

    @Bean
    public UpdatePatientProfilePort updatePatientProfileUseCase(PatientRepositoryPort repository) {
        return new UpdatePatientProfileUseCase(repository);
    }

    @Bean
    public CreatePatientPort createPatientUseCase(PatientRepositoryPort repository,
                                                  IdempotencyKeyRepositoryPort idempotencyRepository) {
        return new CreatePatientUseCase(repository, idempotencyRepository);
    }

    @Bean
    public ListPatientsPort listPatientsUseCase(PatientRepositoryPort repository) {
        return new ListPatientsUseCase(repository);
    }
}