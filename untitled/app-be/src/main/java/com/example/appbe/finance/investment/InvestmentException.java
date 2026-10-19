package com.example.appbe.finance.investment;

import com.example.appbe.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvestmentException extends BusinessException {
    public InvestmentException(String code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }
    public InvestmentException(HttpStatus status, String code, String message) {
        super(status, code, message);
    }
}
