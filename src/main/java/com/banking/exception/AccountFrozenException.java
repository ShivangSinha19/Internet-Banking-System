package com.banking.exception;

public class AccountFrozenException extends Exception {

    private static final long serialVersionUID = 1L;

    public AccountFrozenException(String message) {
        super(message);
    }
}