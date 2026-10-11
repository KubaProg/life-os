package com.example.appbe.finance.overview;

import java.math.BigDecimal;

public record MoneyTotalDto(
        String currency,
        BigDecimal amount
) {
}
