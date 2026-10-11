package com.example.appbe.finance.account;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountDto toDto(FinancialAccount account, BigDecimal currentBalance) {
        return new AccountDto(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getCurrency(),
                currentBalance,
                account.getBalanceStartDate()
        );
    }
}
