package com.banking.controller;

import com.banking.dto.AccountResponse;
import com.banking.dto.CreateAccountRequest;
import com.banking.dto.DepositRequest;
import com.banking.dto.WithdrawRequest;
import com.banking.service.RestBankingService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final RestBankingService service;

    public AccountController(RestBankingService service) { this.service = service; }

    @PostMapping
    public AccountResponse create(Authentication authentication, @Valid @RequestBody CreateAccountRequest request) {
        return service.createAccount(authentication, request);
    }

    @GetMapping("/my")
    public List<AccountResponse> mine(Authentication authentication) { return service.myAccounts(authentication); }

    @GetMapping("/{accountNumber}")
    public AccountResponse get(Authentication authentication, @PathVariable String accountNumber) {
        return service.getAccount(authentication, accountNumber);
    }

    @PostMapping("/{accountNumber}/deposit")
    public AccountResponse deposit(Authentication authentication, @PathVariable String accountNumber,
                                   @Valid @RequestBody DepositRequest request) {
        return service.deposit(authentication, accountNumber, request);
    }

    @PostMapping("/{accountNumber}/withdraw")
    public AccountResponse withdraw(Authentication authentication, @PathVariable String accountNumber,
                                    @Valid @RequestBody WithdrawRequest request) {
        return service.withdraw(authentication, accountNumber, request);
    }
}