package com.example.appbe.finance.investment;

import com.example.appbe.finance.overview.FinanceOverviewDto;

public record InvestmentMutationResponse(Long investmentAccountId, FinanceOverviewDto overview) {}
