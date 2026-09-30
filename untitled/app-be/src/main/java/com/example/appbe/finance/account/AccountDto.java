package com.example.appbe.finance.account;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountDto(
        Long id,
        String name,
        FinancialAccountType type,
        String currency,
        BigDecimal currentBalance,
        LocalDate balanceStartDate
) {
}
