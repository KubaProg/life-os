package com.example.appbe.finance.transfer;
import com.example.appbe.finance.overview.FinanceOverviewDto;
public record TransferResponse(Long transferId, FinanceOverviewDto overview) {}
