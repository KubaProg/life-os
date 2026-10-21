package com.example.appbe.finance.bond;

import com.example.appbe.finance.investment.InvestmentException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** TOS1029, annexes 1 and 2. Rates are percentage points; calculations are per unit first. */
public final class BondCalculator {
    private BondCalculator() {}
    public record Value(BigDecimal principal, BigDecimal grossInterest, BigDecimal fee,
                        BigDecimal tax, BigDecimal netProfit, BigDecimal netValue) {}
    public static BigDecimal capitalized(BondIssue issue, int years) {
        requireRule(issue);
        return issue.getNominal().multiply(BigDecimal.ONE.add(issue.getAnnualRate().movePointLeft(2)).pow(years))
                .setScale(2, RoundingMode.HALF_UP);
    }
    public static BigDecimal unitValue(Bond bond, LocalDate asOf) {
        BondIssue issue = bond.getIssue();
        requireRule(issue);
        if (!asOf.isAfter(bond.getPurchaseDate())) return issue.getNominal();
        if (!asOf.isBefore(bond.getMaturityDate())) return capitalized(issue, issue.getDurationYears());
        int year = 0;
        // Reuse persisted capitalization for current valuations; historical views remain reproducible.
        if (!asOf.isBefore(bond.getPurchaseDate().plusYears(bond.getCapitalizedYears()))) year = bond.getCapitalizedYears();
        while (!asOf.isBefore(bond.getPurchaseDate().plusYears(year + 1))) year++;
        LocalDate from = bond.getPurchaseDate().plusYears(year), until = bond.getPurchaseDate().plusYears(year + 1);
        BigDecimal base = year == bond.getCapitalizedYears() ? bond.getCapitalizedUnitValue() : capitalized(issue, year);
        BigDecimal fraction = BigDecimal.valueOf(ChronoUnit.DAYS.between(from, asOf))
                .divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(from, until)), 18, RoundingMode.HALF_UP);
        return base.multiply(BigDecimal.ONE.add(issue.getAnnualRate().movePointLeft(2).multiply(fraction)))
                .setScale(2, RoundingMode.HALF_UP);
    }
    public static Value calculate(Bond bond, int quantity, LocalDate asOf, boolean early) {
        BigDecimal units = BigDecimal.valueOf(quantity);
        BigDecimal grossUnit = unitValue(bond, asOf).subtract(bond.getIssue().getNominal()).max(BigDecimal.ZERO);
        BigDecimal feeUnit = early ? bond.getIssue().getEarlyRedemptionFee().min(grossUnit) : BigDecimal.ZERO;
        BigDecimal gross = grossUnit.multiply(units), fee = feeUnit.multiply(units);
        BigDecimal principal = bond.getUnitPurchasePrice().multiply(units);
        // One payment for the selected quantity; taxable interest and tax rounded up to cents.
        BigDecimal taxable = gross.subtract(fee).setScale(2, RoundingMode.CEILING);
        BigDecimal tax = taxable.multiply(bond.getIssue().getTaxRate().movePointLeft(2)).setScale(2, RoundingMode.CEILING);
        BigDecimal net = gross.subtract(fee).subtract(tax);
        return new Value(principal, gross, fee, tax, net, principal.add(net));
    }
    private static void requireRule(BondIssue issue) {
        if (issue.getType() != BondType.TOS || !"TOS_FIXED_ANNUAL_V1".equals(issue.getCalculationRule()))
            throw new InvestmentException("UNSUPPORTED_BOND_RULE", "Nieobsługiwane zasady emisji obligacji.");
    }
}
