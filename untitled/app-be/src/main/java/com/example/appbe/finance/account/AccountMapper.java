package com.example.appbe.finance.account;

import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountDto toDto(FinancialAccount account) {
        return new AccountDto(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getCurrency(),
                account.getOpeningBalance(),
                account.getBalanceStartDate()
        );
    }
}
