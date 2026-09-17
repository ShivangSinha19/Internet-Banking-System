package com.banking.exception;

public class InvalidTransferException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidTransferException(String message) {
        super(message);
    }
}