package com.example.appbe.finance.overview;

import com.example.appbe.finance.account.AccountDto;
import com.example.appbe.finance.account.AccountService;
import com.example.appbe.finance.transaction.TransactionMapper;
import com.example.appbe.finance.transaction.TransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FinanceOverviewService {

    private static final Long LOCAL_USER_ID = 1L;

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public FinanceOverviewService(
            AccountService accountService,
            TransactionRepository transactionRepository,
            TransactionMapper transactionMapper
    ) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    public FinanceOverviewDto getOverview() {
        List<AccountDto> accounts = accountService.getActiveAccounts();
        Map<String, BigDecimal> totalsByCurrency = new TreeMap<>();

        for (AccountDto account : accounts) {
            totalsByCurrency.merge(account.currency(), account.currentBalance(), BigDecimal::add);
        }

        List<MoneyTotalDto> accountTotals = totalsByCurrency.entrySet().stream()
                .map(entry -> new MoneyTotalDto(entry.getKey(), entry.getValue()))
                .toList();

        return new FinanceOverviewDto(
                accountTotals,
                accounts,
                transactionRepository.findByUserIdOrderByTransactionDateDescCreatedAtDesc(LOCAL_USER_ID)
                        .stream()
                        .map(transactionMapper::toDto)
                        .toList()
        );
    }
}
