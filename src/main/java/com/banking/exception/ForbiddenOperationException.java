package com.banking.exception;

public class ForbiddenOperationException extends ApiException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}