package com.telemedai.patient.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the Patient Management API.
 *
 * - Stateless (no sessions, no CSRF since this is a token-based API).
 * - `/actuator/health` is public.
 * - Everything else requires an authenticated principal (injected by
 *   `GatewayTrustFilter`).
 * - Method-level authorization is enabled via `@EnableMethodSecurity`;
 *   endpoints declare their required role with `@PreAuthorize`.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            GatewayTrustFilter gatewayTrustFilter
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayTrustFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
