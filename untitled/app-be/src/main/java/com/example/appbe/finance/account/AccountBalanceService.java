package com.example.appbe.finance.account;

import com.example.appbe.finance.transaction.TransactionRepository;
import com.example.appbe.finance.transaction.TransactionType;
import java.math.BigDecimal;
import com.example.appbe.finance.transfer.TransferRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountBalanceService {

    private final TransactionRepository transactionRepository;

    private final TransferRepository transfers;

    public AccountBalanceService(TransactionRepository transactionRepository, TransferRepository transfers) {
        this.transactionRepository = transactionRepository;
        this.transfers = transfers;
    }

    public BigDecimal calculateCurrentBalance(FinancialAccount account) {
        BigDecimal transactionImpact = transactionRepository.sumBalanceImpact(
                account.getUser().getId(),
                account.getId(),
                account.getBalanceStartDate(),
                TransactionType.INCOME,
                TransactionType.EXPENSE
        );

        BigDecimal transferImpact = transfers.sumCashImpact(
                account.getId(), account.getUser().getId(), account.getBalanceStartDate());
        return account.getOpeningBalance().add(transactionImpact).add(transferImpact);
    }
}
