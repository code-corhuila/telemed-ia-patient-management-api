package com.telemedai.patient.infrastructure.adapters.in.rest.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Intercepts all HTTP requests to record the golden signals:
 *   - latency (histogram)
 *   - traffic and errors (counter)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // Run after CorrelationFilter
public class MetricsFilter extends OncePerRequestFilter {

    private final MeterRegistry registry;

    public MetricsFilter(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long start = System.nanoTime();
        int status = 500;
        try {
            chain.doFilter(request, response);
            status = response.getStatus();
        } finally {
            long duration = System.nanoTime() - start;
            
            // Extract path, replacing numbers/UUIDs with a generic token to avoid cardinality explosion
            String path = request.getRequestURI().replaceAll("[0-9a-fA-F-]{36}", "{id}");
            String method = request.getMethod();
            String outcome = status >= 400 ? "error" : "success";

            // Count requests
            Counter.builder("patient_http_requests_total")
                    .description("Total number of HTTP requests")
                    .tag("method", method)
                    .tag("path", path)
                    .tag("status", String.valueOf(status))
                    .tag("outcome", outcome)
                    .register(registry)
                    .increment();

            // Measure latency
            Timer.builder("patient_http_request_duration_seconds")
                    .description("HTTP request latency")
                    .tag("method", method)
                    .tag("path", path)
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(registry)
                    .record(duration, TimeUnit.NANOSECONDS);
        }
    }
}
