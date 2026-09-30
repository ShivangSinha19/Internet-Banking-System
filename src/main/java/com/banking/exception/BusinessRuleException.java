package com.banking.exception;

public class BusinessRuleException extends ApiException {
    public BusinessRuleException(String message) {
        super(message);
    }
}