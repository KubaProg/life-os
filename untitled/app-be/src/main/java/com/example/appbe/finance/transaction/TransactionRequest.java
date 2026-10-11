package com.example.appbe.finance.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull
        Long accountId,

        @NotNull
        TransactionType type,

        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount,

        @NotNull
        @PastOrPresent
        LocalDate transactionDate,

        @NotBlank
        @Size(max = 100)
        String category,

        @Size(max = 500)
        String description
) {
}
