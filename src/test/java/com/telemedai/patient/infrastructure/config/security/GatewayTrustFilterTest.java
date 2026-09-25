package com.telemedai.patient.infrastructure.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class GatewayTrustFilterTest {

    private static final String SECRET = "test-secret";

    private GatewayTrustFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new GatewayTrustFilter(SECRET);
        chain = mock(FilterChain.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilter: should authenticate when the gateway secret and identity are present")
    void shouldAuthenticateWithValidHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(GatewayTrustFilter.HEADER_GATEWAY_SECRET, SECRET);
        request.addHeader(GatewayTrustFilter.HEADER_USER_ID, "1001");
        request.addHeader(GatewayTrustFilter.HEADER_USER_ROLE, "PATIENT");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isInstanceOf(AuthenticatedUser.class);
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        assertThat(user.userId()).isEqualTo(1001L);
        assertThat(user.role()).isEqualTo("PATIENT");

        verify(chain, times(1)).doFilter(any(HttpServletRequest.class),
                any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("doFilter: should not authenticate when the gateway secret is wrong")
    void shouldNotAuthenticateWithWrongSecret() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(GatewayTrustFilter.HEADER_GATEWAY_SECRET, "wrong");
        request.addHeader(GatewayTrustFilter.HEADER_USER_ID, "1001");
        request.addHeader(GatewayTrustFilter.HEADER_USER_ROLE, "PATIENT");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilter: should not authenticate when the identity headers are missing")
    void shouldNotAuthenticateWithoutIdentityHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(GatewayTrustFilter.HEADER_GATEWAY_SECRET, SECRET);
        // No X-User-Id or X-User-Role
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
