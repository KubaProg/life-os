package com.example.appbe.finance.account;

import com.example.appbe.finance.transaction.TransactionDto;
import java.util.List;
import com.example.appbe.finance.transfer.TransferDto;

public record AccountDetailsDto(
        AccountDto account,
        List<TransactionDto> transactions,
        List<TransferDto> transfers
) {
}
