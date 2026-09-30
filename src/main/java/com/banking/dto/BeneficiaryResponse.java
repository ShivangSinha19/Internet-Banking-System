package com.banking.dto;

import com.banking.entity.BeneficiaryEntity;

import java.time.LocalDateTime;

public record BeneficiaryResponse(Integer beneficiaryId, String name, String accountNumber,
                                  String bankName, LocalDateTime createdAt) {
    public static BeneficiaryResponse from(BeneficiaryEntity beneficiary) {
        return new BeneficiaryResponse(beneficiary.getBeneficiaryId(), beneficiary.getName(),
                beneficiary.getAccountNumber(), beneficiary.getBankName(), beneficiary.getCreatedAt());
    }
}