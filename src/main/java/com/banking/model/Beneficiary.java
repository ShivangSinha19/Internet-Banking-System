package com.banking.model;

import java.time.LocalDateTime;

public class Beneficiary {

    private int beneficiaryId;
    private int userId;
    private String name;
    private String accountNumber;
    private String bankName;
    private LocalDateTime createdAt;

    public Beneficiary(int beneficiaryId,
                       int userId,
                       String name,
                       String accountNumber,
                       String bankName,
                       LocalDateTime createdAt) {

        this.beneficiaryId = beneficiaryId;
        this.userId = userId;
        this.name = name;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.createdAt = createdAt;
    }

    public int getBeneficiaryId() {
        return beneficiaryId;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Beneficiary{" +
                "beneficiaryId=" + beneficiaryId +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", bankName='" + bankName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}