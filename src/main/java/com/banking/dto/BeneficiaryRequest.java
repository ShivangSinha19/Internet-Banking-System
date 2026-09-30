package com.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BeneficiaryRequest(@NotBlank @Size(max = 160) String name,
                                 @NotBlank @Size(max = 30) String accountNumber,
                                 @NotBlank @Size(max = 160) String bankName) {
}