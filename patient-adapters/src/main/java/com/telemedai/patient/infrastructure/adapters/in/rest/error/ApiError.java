package com.telemedai.patient.infrastructure.adapters.in.rest.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String error,
        String message,
        List<Detail> details,
        String traceId
) {
    public record Detail(String field, String message) {
    }

    public static ApiError of(String error, String message, String traceId) {
        return new ApiError(error, message, null, traceId);
    }

    public static ApiError of(String error, String message, List<Detail> details, String traceId) {
        return new ApiError(error, message, details, traceId);
    }
}