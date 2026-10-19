package com.example.appbe.finance.overview;

import java.time.YearMonth;
import java.util.List;

public record InvestmentProfitDto(YearMonth month, boolean mocked, List<MoneyTotalDto> totals) {
}
