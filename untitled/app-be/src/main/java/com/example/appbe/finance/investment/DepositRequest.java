package com.example.appbe.finance.investment;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DepositRequest(
        @NotNull UUID requestId,
        @NotBlank @Size(max = 160) String name,
        @NotNull Long sourceAccountId,
        @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 2) BigDecimal principal,
        @NotNull @DecimalMin("0") @DecimalMax("100") @Digits(integer = 3, fraction = 4) BigDecimal annualRate,
        @NotNull @DecimalMin("0") @DecimalMax("100") @Digits(integer = 3, fraction = 4) BigDecimal taxRate,
        @NotNull LocalDate startDate,
        @NotNull LocalDate maturityDate
) {}
