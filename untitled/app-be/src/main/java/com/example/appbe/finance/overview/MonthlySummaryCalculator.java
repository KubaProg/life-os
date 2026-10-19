package com.example.appbe.finance.overview;

import com.example.appbe.finance.account.AccountDto;
import com.example.appbe.finance.transaction.TransactionDto;
import com.example.appbe.finance.transaction.TransactionType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Monthly cashflow calculated independently for each currency. */
public final class MonthlySummaryCalculator {
    private MonthlySummaryCalculator() {}

    public static MonthlySummaryDto summarize(
            YearMonth month, List<AccountDto> accounts, List<TransactionDto> transactions
    ) {
        return summarize(month, accounts, transactions, List.of());
    }

    public static MonthlySummaryDto summarize(YearMonth month, List<AccountDto> accounts,
            List<TransactionDto> transactions, List<MoneyTotalDto> paidInvestmentIncome) {
        Map<String, BigDecimal> income = new TreeMap<>();
        Map<String, BigDecimal> expenses = new TreeMap<>();
        accounts.forEach(account -> income.putIfAbsent(account.currency(), BigDecimal.ZERO));

        for (TransactionDto transaction : transactions) {
            if (!YearMonth.from(transaction.transactionDate()).equals(month)) {
                continue;
            }
            // Transfers, adjustments and future investment valuations are not monthly cashflow.
            if (transaction.type() == TransactionType.INCOME) {
                income.merge(transaction.currency(), transaction.amount(), BigDecimal::add);
            } else if (transaction.type() == TransactionType.EXPENSE) {
                expenses.merge(transaction.currency(), transaction.amount(), BigDecimal::add);
                income.putIfAbsent(transaction.currency(), BigDecimal.ZERO);
            }
        }

        // Only paid investment interest is cash income; accrued valuations stay separate.
        paidInvestmentIncome.forEach(total -> income.merge(total.currency(), total.amount(), BigDecimal::add));

        return new MonthlySummaryDto(month, income.entrySet().stream().map(entry -> {
            BigDecimal monthlyIncome = entry.getValue();
            BigDecimal monthlyExpenses = expenses.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            BigDecimal cashflow = monthlyIncome.subtract(monthlyExpenses);
            // Percentage points: 25.00 means 25%; no income means an undefined rate.
            BigDecimal savingsRate = monthlyIncome.signum() == 0 ? null
                    : cashflow.multiply(BigDecimal.valueOf(100)).divide(monthlyIncome, 2, RoundingMode.HALF_UP);
            return new MonthlySummaryDto.CurrencySummary(
                    entry.getKey(), monthlyIncome, monthlyExpenses, cashflow, savingsRate);
        }).toList());
    }
}
