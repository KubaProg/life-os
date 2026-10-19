package com.example.appbe.finance.overview;

import com.example.appbe.finance.account.AccountDto;
import com.example.appbe.finance.transaction.TransactionDto;
import java.util.List;
import com.example.appbe.finance.investment.InvestmentDto;
import com.example.appbe.finance.transfer.TransferDto;

public record FinanceOverviewDto(
        List<MoneyTotalDto> accountTotals,
        List<AccountDto> accounts,
        List<InvestmentDto> investments,
        List<MoneyTotalDto> investmentTotals,
        List<TransferDto> transfers,
        List<TransactionDto> transactions,
        InvestmentProfitDto investmentProfit,
        MonthlySummaryDto monthlySummary
) {
}
