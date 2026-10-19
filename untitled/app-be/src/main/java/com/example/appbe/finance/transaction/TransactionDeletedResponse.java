package com.example.appbe.finance.transaction;

import com.example.appbe.finance.overview.FinanceOverviewDto;

public record TransactionDeletedResponse(
        Long deletedTransactionId,
        FinanceOverviewDto overview
) {
}
