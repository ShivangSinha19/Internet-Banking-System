package com.banking.controller;

import com.banking.dto.TransactionResponse;
import com.banking.service.RestBankingService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final RestBankingService service;

    public TransactionController(RestBankingService service) { this.service = service; }

    @GetMapping("/my/{accountNumber}")
    public List<TransactionResponse> mine(Authentication authentication, @PathVariable String accountNumber) {
        return service.transactions(authentication, accountNumber);
    }
}