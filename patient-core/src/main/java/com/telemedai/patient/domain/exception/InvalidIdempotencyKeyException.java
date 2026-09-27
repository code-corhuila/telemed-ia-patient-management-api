package com.telemedai.patient.domain.exception;

public class InvalidIdempotencyKeyException extends DomainException {
    public InvalidIdempotencyKeyException(String message) {
        super(message);
    }
}