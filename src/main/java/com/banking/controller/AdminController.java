package com.banking.controller;

import com.banking.dto.AccountResponse;
import com.banking.dto.TransactionResponse;
import com.banking.dto.UserResponse;
import com.banking.service.RestBankingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final RestBankingService service;

    public AdminController(RestBankingService service) { this.service = service; }

    @GetMapping("/users")
    public List<UserResponse> users() { return service.allUsers(); }

    @GetMapping("/accounts")
    public List<AccountResponse> accounts() { return service.allAccounts(); }

    @GetMapping("/transactions")
    public List<TransactionResponse> transactions() { return service.allTransactions(); }

    @PatchMapping("/accounts/{accountNumber}/freeze")
    public AccountResponse freeze(@PathVariable String accountNumber) {
        return service.changeAccountStatus(accountNumber, true);
    }

    @PatchMapping("/accounts/{accountNumber}/unfreeze")
    public AccountResponse unfreeze(@PathVariable String accountNumber) {
        return service.changeAccountStatus(accountNumber, false);
    }
}