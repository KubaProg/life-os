package com.example.appbe.finance.investment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvestmentOperationDto(Long id, Long transferId, String type, LocalDate date,
                                     String currency, BigDecimal amount) {
    public static InvestmentOperationDto from(InvestmentOperation operation) {
        return new InvestmentOperationDto(operation.getId(), operation.getTransfer().getId(), operation.getType(),
                operation.getOperationDate(), operation.investmentCurrency(), operation.getAmount());
    }
}
