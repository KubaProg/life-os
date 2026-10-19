package com.example.appbe.finance.investment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Fixed-rate, simple interest, ACT/ACT by calendar year; no interim capitalization. */
public final class DepositCalculator {
    private DepositCalculator() {}
    public record Value(BigDecimal grossProfit, BigDecimal estimatedTax,
                        BigDecimal netProfit, BigDecimal currentValue) {}

    public static Value calculate(BigDecimal principal, BigDecimal annualRate, BigDecimal taxRate,
                                  LocalDate start, LocalDate maturity, LocalDate asOf) {
        LocalDate end = asOf.isBefore(start) ? start : asOf.isAfter(maturity) ? maturity : asOf;
        BigDecimal yearFraction = BigDecimal.ZERO;
        // Completed days in [start, end), split at year boundaries (365 or 366).
        for (LocalDate date = start; date.isBefore(end); ) {
            LocalDate nextYear = LocalDate.of(date.getYear() + 1, 1, 1);
            LocalDate segmentEnd = nextYear.isBefore(end) ? nextYear : end;
            yearFraction = yearFraction.add(BigDecimal.valueOf(ChronoUnit.DAYS.between(date, segmentEnd))
                    .divide(BigDecimal.valueOf(date.lengthOfYear()), 16, RoundingMode.HALF_UP));
            date = segmentEnd;
        }
        BigDecimal rawGross = principal.multiply(annualRate).multiply(yearFraction)
                .divide(BigDecimal.valueOf(100), 16, RoundingMode.HALF_UP);
        // Interim values retain four decimals. At maturity the payable interest uses cents.
        BigDecimal gross = rawGross.setScale(end.equals(maturity) ? 2 : 4, RoundingMode.HALF_UP);
        BigDecimal tax = gross.multiply(taxRate).divide(BigDecimal.valueOf(100), end.equals(maturity) ? 2 : 4, RoundingMode.HALF_UP);
        BigDecimal net = gross.subtract(tax);
        return new Value(gross, tax, net, principal.add(net));
    }

    public static Value calculate(InvestmentAccount investment, LocalDate asOf) {
        DepositTerms terms = investment.getDepositTerms();
        return calculate(investment.getPrincipal(), terms.getAnnualRate(), terms.getTaxRate(),
                terms.getStartDate(), terms.getMaturityDate(), asOf);
    }
}
