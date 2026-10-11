package com.example.appbe.finance.account;

import com.example.appbe.finance.transaction.TransactionRepository;
import com.example.appbe.finance.transaction.TransactionType;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class AccountBalanceService {

    private final TransactionRepository transactionRepository;

    public AccountBalanceService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public BigDecimal calculateCurrentBalance(FinancialAccount account) {
        BigDecimal transactionImpact = transactionRepository.sumBalanceImpact(
                account.getUser().getId(),
                account.getId(),
                account.getBalanceStartDate(),
                TransactionType.INCOME,
                TransactionType.EXPENSE
        );

        return account.getOpeningBalance().add(transactionImpact);
    }
}
