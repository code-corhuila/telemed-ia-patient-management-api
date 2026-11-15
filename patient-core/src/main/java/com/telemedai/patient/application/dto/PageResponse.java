package com.telemedai.patient.application.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> data,
        Meta meta
) {
    public record Meta(int page, int limit, long total, int totalPages) {
        public static Meta of(int page, int limit, long total) {
            int totalPages = limit > 0 ? (int) Math.ceil((double) total / limit) : 0;
            return new Meta(page, limit, total, totalPages);
        }
    }
}