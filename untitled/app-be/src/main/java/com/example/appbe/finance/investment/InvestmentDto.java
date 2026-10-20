package com.example.appbe.finance.investment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvestmentDto(
        Long id, String name, String type, String currency,
        BigDecimal principal, BigDecimal currentValue, BigDecimal grossProfit,
        BigDecimal estimatedTax, BigDecimal netProfit, BigDecimal annualRate, BigDecimal taxRate,
        String calculationRule, String capitalization, LocalDate startDate, LocalDate maturityDate,
        LocalDate valuedOn, String status
) {
    public static InvestmentDto cashAccount(Long id, String name, String type, String currency,
            BigDecimal cashBalance, LocalDate today) {
        BigDecimal zero = BigDecimal.ZERO;
        return new InvestmentDto(id, name, type, currency, zero, cashBalance, zero, zero, zero,
                null, null, null, null, null, null, today, "READY");
    }
}
