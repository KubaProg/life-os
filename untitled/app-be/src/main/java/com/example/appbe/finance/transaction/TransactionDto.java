package com.example.appbe.finance.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionDto(
        Long id,
        Long accountId,
        String accountName,
        TransactionType type,
        BigDecimal amount,
        String currency,
        LocalDate transactionDate,
        String category,
        String description,
        boolean accountDeleted
) {
    public TransactionDto(Long id, Long accountId, String accountName, TransactionType type, BigDecimal amount,
            String currency, LocalDate transactionDate, String category, String description) {
        this(id, accountId, accountName, type, amount, currency, transactionDate, category, description, false);
    }
}
