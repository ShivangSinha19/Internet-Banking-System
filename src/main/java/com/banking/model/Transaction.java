package com.banking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private int transactionId;
    private int fromAccountId;
    private int toAccountId;
    private BigDecimal amount;
    private String transactionType;
    private LocalDateTime transactionDate;
    private String description;

    public Transaction(int transactionId,
                       int fromAccountId,
                       int toAccountId,
                       BigDecimal amount,
                       String transactionType,
                       LocalDateTime transactionDate,
                       String description) {

        this.transactionId = transactionId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.description = description;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getFromAccountId() {
        return fromAccountId;
    }

    public int getToAccountId() {
        return toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", fromAccountId=" + fromAccountId +
                ", toAccountId=" + toAccountId +
                ", amount=" + amount +
                ", transactionType='" + transactionType + '\'' +
                ", transactionDate=" + transactionDate +
                ", description='" + description + '\'' +
                '}';
    }
}