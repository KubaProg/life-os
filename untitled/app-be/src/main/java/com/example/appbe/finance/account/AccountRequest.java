package com.example.appbe.finance.account;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountRequest(
        @NotBlank
        @Size(max = 255)
        String name,

        @NotNull
        FinancialAccountType type,

        @NotBlank
        @Pattern(regexp = "[A-Z]{3}")
        String currency,

        @NotNull
        @Digits(integer = 15, fraction = 4)
        BigDecimal openingBalance,

        @NotNull
        LocalDate balanceStartDate
) {
}
