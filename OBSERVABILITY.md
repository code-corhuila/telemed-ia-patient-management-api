# Observability Implementation — Patient Management API

This document maps the Week 11 Workshop (CP1-CP4) concepts to this service.

## CP1 & CP2: Tracing and Structured Logs
- **Correlation ID:** The `CorrelationFilter` reads or generates `X-Correlation-Id`, stores it in the SLF4J MDC, and echoes it back.
- **Structured logs:** `logback-spring.xml` outputs JSON to stdout, automatically including the `correlationId` from the MDC. All logs are ready to be ingested by Loki or ELK.

## CP3: Instrumentation Code
- **Metrics Filter:** `MetricsFilter` wraps every HTTP request. It extracts the path, method, status, and duration.
- **Endpoint:** `/actuator/prometheus` is exposed and permitted in `SecurityConfig` for scraping.

## CP4: Golden Signals and Metric Catalog
| Signal | Metric Name | Type | Labels | PromQL (Dashboard) |
|---|---|---|---|---|
| **Latency** | `patient_http_request_duration_seconds` | Timer (Histogram) | method, path | `histogram_quantile(0.95, sum by (le) (rate(patient_http_request_duration_seconds_bucket[5m])))` |
| **Traffic** | `patient_http_requests_total` | Counter | method, path, status, outcome | `sum(rate(patient_http_requests_total[5m])) by (path)` |
| **Errors** | `patient_http_requests_total` | Counter | method, path, status, outcome | `sum(rate(patient_http_requests_total{outcome="error"}[5m]))` |

**Buckets:** The latency timer uses defaults + percentiles (0.5, 0.95, 0.99). For the workshop, we can define custom `serviceLevelObjectives` if needed.
