package com.banking.exception;

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(message);
    }
}