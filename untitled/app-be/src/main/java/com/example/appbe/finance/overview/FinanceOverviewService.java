package com.example.appbe.finance.overview;

import com.example.appbe.finance.account.AccountDto;
import com.example.appbe.finance.account.AccountService;
import com.example.appbe.finance.transaction.TransactionMapper;
import com.example.appbe.finance.transaction.TransactionDto;
import com.example.appbe.finance.transaction.TransactionRepository;
import java.math.BigDecimal;
import com.example.appbe.finance.investment.InvestmentQueryService;
import com.example.appbe.finance.transfer.TransferRepository;
import com.example.appbe.finance.transfer.TransferDto;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FinanceOverviewService {

    private static final Long LOCAL_USER_ID = 1L;
    private static final ZoneId USER_ZONE = ZoneId.of("Europe/Warsaw");

    private final TransferRepository transfers;
    private final InvestmentQueryService investmentQueries;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public FinanceOverviewService(
            TransferRepository transfers,
            InvestmentQueryService investmentQueries,
            AccountService accountService,
            TransactionRepository transactionRepository,
            TransactionMapper transactionMapper
    ) {
        this.transfers = transfers;
        this.investmentQueries = investmentQueries;
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    public FinanceOverviewDto getOverview() {
        return getOverview(null);
    }

    public FinanceOverviewDto getOverview(YearMonth month) {
        LocalDate today = LocalDate.now(USER_ZONE);
        YearMonth selectedMonth = month == null ? YearMonth.from(today) : month;
        InvestmentQueryService.Snapshot investments = investmentQueries.snapshot(selectedMonth);
        List<AccountDto> accounts = accountService.getActiveAccounts();
        Map<String, BigDecimal> totalsByCurrency = new TreeMap<>();

        for (AccountDto account : accounts) {
            totalsByCurrency.merge(account.currency(), account.currentBalance(), BigDecimal::add);
        }

        List<MoneyTotalDto> accountTotals = totalsByCurrency.entrySet().stream()
                .map(entry -> new MoneyTotalDto(entry.getKey(), entry.getValue()))
                .toList();

        List<TransactionDto> transactions = transactionRepository
                .findByUserIdOrderByTransactionDateDescCreatedAtDesc(LOCAL_USER_ID)
                .stream().map(transactionMapper::toDto).toList();

        return new FinanceOverviewDto(
                accountTotals,
                accounts,
                investments.investments(),
                investments.totals(),
                transfers.findByUserIdOrderByDateDescIdDesc(LOCAL_USER_ID).stream().map(TransferDto::from).toList(),
                transactions,
                investments.profit(),
                MonthlySummaryCalculator.summarize(selectedMonth, accounts, transactions, investments.paidIncome())
        );
    }

}
