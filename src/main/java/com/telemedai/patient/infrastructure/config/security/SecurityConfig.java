package com.telemedai.patient.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Minimal Spring Security configuration.
 *
 * - Stateless (no sessions, no CSRF since this is a token-based API).
 * - `/actuator/health` is public.
 * - Everything else requires an authenticated principal (injected by
 *   `GatewayTrustFilter`).
 *
 * PR #7b will expand this with formal role-based authorization and tests.
 */
@Configuration
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
