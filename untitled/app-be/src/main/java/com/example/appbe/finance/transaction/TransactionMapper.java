package com.example.appbe.finance.transaction;

import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionDto toDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getFinancialAccount().getId(),
                transaction.getFinancialAccount().getName(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransactionDate(),
                transaction.getCategory(),
                transaction.getDescription()
        );
    }
}
