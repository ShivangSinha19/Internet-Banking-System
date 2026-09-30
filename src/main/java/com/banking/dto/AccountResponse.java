package com.banking.dto;

import com.banking.entity.AccountEntity;

import java.math.BigDecimal;

public record AccountResponse(Integer accountId, String accountNumber, String accountType,
                              BigDecimal balance, String status) {
    public static AccountResponse from(AccountEntity account) {
        return new AccountResponse(account.getAccountId(), account.getAccountNumber(),
                account.getAccountType(), account.getBalance(), account.getStatus());
    }
}