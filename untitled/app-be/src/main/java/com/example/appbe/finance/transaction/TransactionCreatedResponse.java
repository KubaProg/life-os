package com.example.appbe.finance.transaction;

import com.example.appbe.finance.overview.FinanceOverviewDto;

public record TransactionCreatedResponse(
        TransactionDto createdTransaction,
        FinanceOverviewDto overview
) {
}
