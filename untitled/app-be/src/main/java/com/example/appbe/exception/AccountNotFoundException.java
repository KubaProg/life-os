package com.example.appbe.exception;

import org.springframework.http.HttpStatus;

public class AccountNotFoundException extends BusinessException {

    public AccountNotFoundException(Long accountId) {
        super(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Financial account not found: " + accountId);
    }
}
