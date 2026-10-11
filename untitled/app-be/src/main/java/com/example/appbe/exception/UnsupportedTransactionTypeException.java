package com.example.appbe.exception;

import com.example.appbe.finance.transaction.TransactionType;
import org.springframework.http.HttpStatus;

public class UnsupportedTransactionTypeException extends BusinessException {

    public UnsupportedTransactionTypeException(TransactionType type) {
        super(
                HttpStatus.BAD_REQUEST,
                "UNSUPPORTED_TRANSACTION_TYPE",
                "Only INCOME and EXPENSE transactions are supported: " + type
        );
    }
}
