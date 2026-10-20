package com.example.appbe.finance.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferDto(Long id, AccountReference source, String sourceName,
                          AccountReference destination, String destinationName,
                          BigDecimal amount, String currency, LocalDate date) {
    public static TransferDto from(MoneyTransfer transfer) {
        return new TransferDto(transfer.getId(), transfer.sourceReference(), transfer.sourceName(),
                transfer.destinationReference(), transfer.destinationName(),
                transfer.getAmount(), transfer.getCurrency(), transfer.getDate());
    }
}
