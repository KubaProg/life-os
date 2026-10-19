package com.example.appbe.exception;

import org.springframework.http.HttpStatus;

public class TransactionNotFoundException extends BusinessException {

    public TransactionNotFoundException(Long transactionId) {
        super(HttpStatus.NOT_FOUND, "TRANSACTION_NOT_FOUND", "Transaction not found: " + transactionId);
    }
}
