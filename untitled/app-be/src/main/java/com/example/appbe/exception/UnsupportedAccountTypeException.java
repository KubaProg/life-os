package com.example.appbe.exception;

import com.example.appbe.finance.account.FinancialAccountType;
import org.springframework.http.HttpStatus;

public class UnsupportedAccountTypeException extends BusinessException {

    public UnsupportedAccountTypeException(FinancialAccountType type) {
        super(
                HttpStatus.BAD_REQUEST,
                "UNSUPPORTED_ACCOUNT_TYPE",
                "Unsupported account type for the first account version: " + type
        );
    }
}
