package com.example.appbe.finance.account;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OpeningBalanceRequest(
        @NotNull @Digits(integer = 15, fraction = 4) BigDecimal openingBalance,
        @NotNull LocalDate balanceStartDate
) {}
