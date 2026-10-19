package com.example.appbe.finance.overview;

import com.example.appbe.finance.account.AccountDto;
import com.example.appbe.finance.account.FinancialAccountType;
import com.example.appbe.finance.transaction.TransactionDto;
import com.example.appbe.finance.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/** Dependency-free regression checks; can run with javac/java without Maven or a database. */
public class MonthlySummaryCalculatorTest {
    public static void main(String[] args) {
        var accounts = List.of(new AccountDto(1L, "PLN", FinancialAccountType.BANK_ACCOUNT,
                "PLN", BigDecimal.ZERO, LocalDate.of(2026, 1, 1)));
        var transactions = List.of(
                transaction(TransactionType.INCOME, "1000", "PLN", "2026-10-01"),
                transaction(TransactionType.EXPENSE, "250", "PLN", "2026-10-31"),
                transaction(TransactionType.TRANSFER, "9000", "PLN", "2026-10-16"),
                transaction(TransactionType.ADJUSTMENT, "8000", "PLN", "2026-10-16"),
                transaction(TransactionType.INCOME, "7000", "PLN", "2026-09-30"),
                transaction(TransactionType.EXPENSE, "6000", "PLN", "2026-11-01"),
                transaction(TransactionType.EXPENSE, "25", "EUR", "2026-10-16"),
                transaction(TransactionType.INCOME, "3", "USD", "2026-10-16"),
                transaction(TransactionType.EXPENSE, "4", "USD", "2026-10-16")
        );
        var summary = MonthlySummaryCalculator.summarize(YearMonth.of(2026, 10), accounts, transactions);
        if (summary.totals().size() != 3) throw new AssertionError("Currencies must stay separate");
        var pln = summary.totals().stream().filter(total -> total.currency().equals("PLN")).findFirst().orElseThrow();
        equal("1000", pln.income()); equal("250", pln.expenses());
        equal("750", pln.cashflow()); equal("75", pln.savingsRate());
        var eur = summary.totals().stream().filter(total -> total.currency().equals("EUR")).findFirst().orElseThrow();
        equal("-25", eur.cashflow());
        if (eur.savingsRate() != null) throw new AssertionError("No income must produce null savings rate");
        var usd = summary.totals().stream().filter(total -> total.currency().equals("USD")).findFirst().orElseThrow();
        equal("-1", usd.cashflow()); equal("-33.33", usd.savingsRate());
        var empty = MonthlySummaryCalculator.summarize(YearMonth.of(2026, 12), accounts, transactions).totals().get(0);
        equal("0", empty.income()); equal("0", empty.expenses()); equal("0", empty.cashflow());
        if (empty.savingsRate() != null) throw new AssertionError("Empty month must produce null savings rate");
        if (!MonthlySummaryCalculator.summarize(YearMonth.of(2026, 10), List.of(), List.of()).totals().isEmpty()) {
            throw new AssertionError("No accounts or transactions must produce an empty summary");
        }
        System.out.println("Monthly summary checks passed: month boundaries, transfers, adjustments, currencies, empty month, no income, negative cashflow and rounding.");
    }

    private static TransactionDto transaction(TransactionType type, String amount, String currency, String date) {
        return new TransactionDto(1L, 1L, "Test", type, new BigDecimal(amount), currency,
                LocalDate.parse(date), "Test", null);
    }

    private static void equal(String expected, BigDecimal actual) {
        if (actual == null || new BigDecimal(expected).compareTo(actual) != 0) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }
}
