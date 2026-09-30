package com.banking.controller;

import com.banking.dto.BeneficiaryRequest;
import com.banking.dto.BeneficiaryResponse;
import com.banking.service.RestBankingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {
    private final RestBankingService service;

    public BeneficiaryController(RestBankingService service) { this.service = service; }

    @PostMapping
    public BeneficiaryResponse add(Authentication authentication, @Valid @RequestBody BeneficiaryRequest request) {
        return service.addBeneficiary(authentication, request);
    }

    @GetMapping("/my")
    public List<BeneficiaryResponse> mine(Authentication authentication) { return service.myBeneficiaries(authentication); }

    @DeleteMapping("/{beneficiaryId}")
    public ResponseEntity<Void> remove(Authentication authentication, @PathVariable Integer beneficiaryId) {
        service.removeBeneficiary(authentication, beneficiaryId);
        return ResponseEntity.noContent().build();
    }
}