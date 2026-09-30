package com.banking.controller;

import com.banking.dto.TransferRequest;
import com.banking.service.RestBankingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final RestBankingService service;

    public TransferController(RestBankingService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Void> transfer(Authentication authentication, @Valid @RequestBody TransferRequest request) {
        service.transfer(authentication, request);
        return ResponseEntity.noContent().build();
    }
}