package com.example.appbe.finance.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferDto(Long id, AccountReference source, String sourceName,
                          AccountReference destination, String destinationName,
                          BigDecimal amount, String currency, LocalDate date) {
    public static TransferDto from(MoneyTransfer transfer) {
        var source = transfer.getSourceAccount();
        var destination = transfer.getDestinationAccount();
        return new TransferDto(transfer.getId(), new AccountReference(source.getKind(), source.getId()), source.getName(),
                new AccountReference(destination.getKind(), destination.getId()), destination.getName(),
                transfer.getAmount(), transfer.getCurrency(), transfer.getDate());
    }
}
