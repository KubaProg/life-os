package com.example.appbe.finance.overview;

import com.example.appbe.finance.account.AccountDto;
import com.example.appbe.finance.transaction.TransactionDto;
import java.util.List;

public record FinanceOverviewDto(
        List<MoneyTotalDto> accountTotals,
        List<AccountDto> accounts,
        List<TransactionDto> transactions
) {
}
