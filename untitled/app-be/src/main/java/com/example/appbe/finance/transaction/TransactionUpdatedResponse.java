package com.example.appbe.finance.transaction;

import com.example.appbe.finance.overview.FinanceOverviewDto;

public record TransactionUpdatedResponse(
        TransactionDto updatedTransaction,
        FinanceOverviewDto overview
) {
}
