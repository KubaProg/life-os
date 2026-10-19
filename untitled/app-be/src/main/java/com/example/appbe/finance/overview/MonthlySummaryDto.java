package com.example.appbe.finance.overview;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record MonthlySummaryDto(YearMonth month, List<CurrencySummary> totals) {
    public record CurrencySummary(
            String currency,
            BigDecimal income,
            BigDecimal expenses,
            BigDecimal cashflow,
            BigDecimal savingsRate
    ) {}
}
