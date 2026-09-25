package com.telemedai.patient.infrastructure.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Temporary trust filter that:
 *
 * 1. Verifies the request came from the API Gateway by checking a shared
 *    secret header (`X-Gateway-Secret`). Without this, a client could hit
 *    this service directly and spoof `X-User-Id`.
 * 2. Extracts `X-User-Id` and `X-User-Role` and populates the Spring Security
 *    context so downstream code can rely on `@AuthenticationPrincipal`.
 *
 * This is intentionally minimal. PR #7b will replace this with a proper
 * Spring Security configuration (full `SecurityFilterChain`, role-based
 * authorization per endpoint, and tests).
 */
@Component
public class GatewayTrustFilter extends OncePerRequestFilter {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";
    public static final String HEADER_GATEWAY_SECRET = "X-Gateway-Secret";

    private final String expectedGatewaySecret;

    public GatewayTrustFilter(
            @Value("${app.security.gateway-secret}") String expectedGatewaySecret
    ) {
        this.expectedGatewaySecret = expectedGatewaySecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Reject requests that did not pass through the API Gateway.
        String providedSecret = request.getHeader(HEADER_GATEWAY_SECRET);
        if (!expectedGatewaySecret.equals(providedSecret)) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = request.getHeader(HEADER_USER_ID);
        String role = request.getHeader(HEADER_USER_ROLE);

        if (userId != null && role != null) {
            AuthenticatedUser principal = new AuthenticatedUser(
                    Long.parseLong(userId),
                    role
            );
            var auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
