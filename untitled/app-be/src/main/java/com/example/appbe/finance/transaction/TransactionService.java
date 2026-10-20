package com.example.appbe.finance.transaction;

import com.example.appbe.exception.AccountNotFoundException;
import com.example.appbe.exception.TransactionBeforeBalanceStartDateException;
import com.example.appbe.exception.TransactionNotFoundException;
import com.example.appbe.exception.UnsupportedTransactionTypeException;
import com.example.appbe.finance.account.FinancialAccount;
import com.example.appbe.finance.account.FinancialAccountRepository;
import com.example.appbe.finance.overview.FinanceOverviewService;
import com.example.appbe.user.User;
import com.example.appbe.user.UserRepository;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private static final Long LOCAL_USER_ID = 1L;
    private static final Set<TransactionType> SUPPORTED_TYPES = EnumSet.of(
            TransactionType.INCOME,
            TransactionType.EXPENSE
    );

    private final TransactionRepository transactionRepository;
    private final FinancialAccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;
    private final FinanceOverviewService overviewService;

    public TransactionService(
            TransactionRepository transactionRepository,
            FinancialAccountRepository accountRepository,
            UserRepository userRepository,
            TransactionMapper transactionMapper,
            FinanceOverviewService overviewService
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionMapper = transactionMapper;
        this.overviewService = overviewService;
    }

    @Transactional
    public TransactionCreatedResponse createTransaction(TransactionRequest request, YearMonth month) {
        userRepository.findForUpdate(LOCAL_USER_ID).orElseThrow();
        FinancialAccount account = validateRequest(request);

        User user = userRepository.getReferenceById(LOCAL_USER_ID);
        Transaction transaction = new Transaction(
                user,
                account,
                request.type(),
                request.amount(),
                account.getCurrency(),
                request.transactionDate(),
                request.category().trim(),
                trimToNull(request.description())
        );

        Transaction savedTransaction = transactionRepository.save(transaction);

        return new TransactionCreatedResponse(
                transactionMapper.toDto(savedTransaction),
                overviewService.getOverview(month)
        );
    }

    @Transactional
    public TransactionUpdatedResponse updateTransaction(Long transactionId, TransactionRequest request, YearMonth month) {
        userRepository.findForUpdate(LOCAL_USER_ID).orElseThrow();
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, LOCAL_USER_ID)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
        validateType(transaction.getType());
        FinancialAccount account = validateRequest(request);
        if (transaction.isAccountDeleted()) {
            throw new com.example.appbe.finance.investment.InvestmentException("ACCOUNT_DELETED", "Nie można edytować transakcji usuniętego konta.");
        }

        transaction.setFinancialAccount(account);
        transaction.setType(request.type());
        transaction.setAmount(request.amount());
        transaction.setCurrency(account.getCurrency());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setCategory(request.category().trim());
        transaction.setDescription(trimToNull(request.description()));

        // Flush before calculating the snapshot so both accounts reflect the updated transaction.
        Transaction savedTransaction = transactionRepository.saveAndFlush(transaction);
        return new TransactionUpdatedResponse(
                transactionMapper.toDto(savedTransaction),
                overviewService.getOverview(month)
        );
    }

    @Transactional
    public TransactionDeletedResponse deleteTransaction(Long transactionId, YearMonth month) {
        userRepository.findForUpdate(LOCAL_USER_ID).orElseThrow();
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, LOCAL_USER_ID)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
        validateType(transaction.getType());

        transactionRepository.delete(transaction);
        // Apply the deletion before calculating balances and transaction history.
        transactionRepository.flush();
        return new TransactionDeletedResponse(transactionId, overviewService.getOverview(month));
    }

    private FinancialAccount validateRequest(TransactionRequest request) {
        validateType(request.type());
        FinancialAccount account = accountRepository
                .findByIdAndUserIdAndActiveTrue(request.accountId(), LOCAL_USER_ID)
                .orElseThrow(() -> new AccountNotFoundException(request.accountId()));
        if (request.transactionDate().isBefore(account.getBalanceStartDate())) {
            throw new TransactionBeforeBalanceStartDateException(account.getBalanceStartDate());
        }
        return account;
    }

    private void validateType(TransactionType type) {
        if (!SUPPORTED_TYPES.contains(type)) {
            throw new UnsupportedTransactionTypeException(type);
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
