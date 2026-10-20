package com.example.appbe.finance.account;

import com.example.appbe.finance.overview.FinanceOverviewDto;

public record AccountDeletedResponse(Long deletedAccountId, FinanceOverviewDto overview) {}
