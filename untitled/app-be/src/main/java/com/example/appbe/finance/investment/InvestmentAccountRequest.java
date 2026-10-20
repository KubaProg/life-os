package com.example.appbe.finance.investment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InvestmentAccountRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
        @NotNull InvestmentType type
) {}
