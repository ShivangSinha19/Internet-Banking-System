package com.banking.dto;

import com.banking.entity.TransactionEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(Long transactionId, String fromAccountNumber, String toAccountNumber,
                                  BigDecimal amount, String transactionType, LocalDateTime transactionDate,
                                  String description) {
    public static TransactionResponse from(TransactionEntity transaction) {
        return new TransactionResponse(transaction.getTransactionId(),
                transaction.getFromAccount() == null ? null : transaction.getFromAccount().getAccountNumber(),
                transaction.getToAccount() == null ? null : transaction.getToAccount().getAccountNumber(),
                transaction.getAmount(), transaction.getTransactionType(), transaction.getTransactionDate(),
                transaction.getDescription());
    }
}