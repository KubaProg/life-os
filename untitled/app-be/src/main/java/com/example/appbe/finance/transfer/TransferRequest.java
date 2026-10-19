package com.example.appbe.finance.transfer;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransferRequest(
        @NotNull UUID requestId,
        @NotNull @Positive Long sourceAccountId,
        @NotNull @Positive Long destinationAccountId,
        @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 2) BigDecimal amount,
        @NotNull LocalDate date
) {}
