package com.example.appbe.finance.account;

import com.example.appbe.finance.transaction.TransactionDto;
import java.util.List;

public record AccountDetailsDto(
        AccountDto account,
        List<TransactionDto> transactions
) {
}
