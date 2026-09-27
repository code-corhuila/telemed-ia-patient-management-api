package com.telemedai.patient.domain.exception;

public class InvalidPaginationException extends DomainException {
    public InvalidPaginationException(String message) {
        super(message);
    }
}