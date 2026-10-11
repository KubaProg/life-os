package com.example.appbe.exception;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;

public class TransactionBeforeBalanceStartDateException extends BusinessException {

    public TransactionBeforeBalanceStartDateException(LocalDate balanceStartDate) {
        super(
                HttpStatus.BAD_REQUEST,
                "TRANSACTION_BEFORE_BALANCE_START_DATE",
                "Transaction date cannot be before account balance start date: " + balanceStartDate
        );
    }
}
