package com.example.appbe.finance.account;

import com.example.appbe.exception.AccountNotFoundException;
import com.example.appbe.exception.UnsupportedAccountTypeException;
import com.example.appbe.finance.transaction.TransactionMapper;
import com.example.appbe.finance.transaction.TransactionRepository;
import com.example.appbe.user.User;
import com.example.appbe.user.UserRepository;
import com.example.appbe.finance.transfer.TransferRepository;
import com.example.appbe.finance.transfer.TransferDto;
import com.example.appbe.finance.investment.InvestmentException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountService {

    private static final Long LOCAL_USER_ID = 1L;
    private static final Set<FinancialAccountType> SUPPORTED_ACCOUNT_TYPES = EnumSet.of(
            FinancialAccountType.BANK_ACCOUNT,
            FinancialAccountType.SAVINGS,
            FinancialAccountType.CASH
    );

    private final FinancialAccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransferRepository transfers;
    private final AccountMapper accountMapper;
    private final AccountBalanceService accountBalanceService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public AccountService(
            FinancialAccountRepository accountRepository,
            UserRepository userRepository,
            TransferRepository transfers,
            AccountMapper accountMapper,
            AccountBalanceService accountBalanceService,
            TransactionRepository transactionRepository,
            TransactionMapper transactionMapper
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transfers = transfers;
        this.accountMapper = accountMapper;
        this.accountBalanceService = accountBalanceService;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    public List<AccountDto> getActiveAccounts() {
        return accountRepository.findByUserIdAndActiveTrueOrderByNameAsc(LOCAL_USER_ID).stream()
                .map(this::toDto)
                .toList();
    }

    public AccountDto getActiveAccount(Long accountId) {
        return toDto(getActiveAccountEntity(accountId));
    }

    public AccountDetailsDto getActiveAccountDetails(Long accountId) {
        FinancialAccount account = getActiveAccountEntity(accountId);

        return new AccountDetailsDto(
                toDto(account),
                transactionRepository
                        .findByUserIdAndFinancialAccountIdOrderByTransactionDateDescCreatedAtDesc(
                                LOCAL_USER_ID,
                                accountId
                        )
                        .stream()
                        .map(transactionMapper::toDto)
                        .toList(),
                transfers.findByUserIdOrderByDateDescIdDesc(LOCAL_USER_ID).stream()
                        .map(TransferDto::from).filter(transfer ->
                                transfer.source().id().equals(accountId) || transfer.destination().id().equals(accountId)).toList()
        );
    }

    @Transactional
    public AccountDto createAccount(AccountRequest request) {
        validateAccountType(request.type());
        User user = userRepository.getReferenceById(LOCAL_USER_ID);
        FinancialAccount account = new FinancialAccount(
                user,
                request.name().trim(),
                request.type(),
                request.currency(),
                request.openingBalance(),
                request.balanceStartDate()
        );

        FinancialAccount savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount, savedAccount.getOpeningBalance());
    }

    @Transactional
    public AccountDto updateAccount(Long accountId, UpdateAccountRequest request) {
        userRepository.findForUpdate(LOCAL_USER_ID).orElseThrow();
        if (request.type() != null) {
            validateAccountType(request.type());
        }

        FinancialAccount account = getActiveAccountEntity(accountId);

        if (request.name() != null) {
            account.setName(request.name().trim());
        }
        if (request.type() != null) {
            account.setType(request.type());
        }
        if (request.currency() != null) {
            if (!request.currency().equals(account.getCurrency()) && transfers.countForAccount(accountId) > 0) {
                throw new InvestmentException("INVESTMENT_ACCOUNT_CURRENCY_LOCKED", "Nie można zmienić waluty konta z transferami.");
            }
            account.setCurrency(request.currency());
        }

        return toDto(account);
    }

    private void validateAccountType(FinancialAccountType type) {
        if (!SUPPORTED_ACCOUNT_TYPES.contains(type)) {
            throw new UnsupportedAccountTypeException(type);
        }
    }

    private FinancialAccount getActiveAccountEntity(Long accountId) {
        return accountRepository.findByIdAndUserIdAndActiveTrue(accountId, LOCAL_USER_ID)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private AccountDto toDto(FinancialAccount account) {
        return accountMapper.toDto(account, accountBalanceService.calculateCurrentBalance(account));
    }
}
