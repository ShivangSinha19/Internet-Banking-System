package com.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(@NotBlank @Size(max = 30) String accountNumber,
                                   @NotBlank @Size(max = 30) String accountType) {
}