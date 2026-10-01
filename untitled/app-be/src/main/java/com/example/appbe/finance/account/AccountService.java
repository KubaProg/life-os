package com.example.appbe.finance.account;

import com.example.appbe.exception.AccountNotFoundException;
import com.example.appbe.exception.UnsupportedAccountTypeException;
import com.example.appbe.user.User;
import com.example.appbe.user.UserRepository;
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
    private final AccountMapper accountMapper;

    public AccountService(
            FinancialAccountRepository accountRepository,
            UserRepository userRepository,
            AccountMapper accountMapper
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountMapper = accountMapper;
    }

    public List<AccountDto> getActiveAccounts() {
        return accountRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(accountMapper::toDto)
                .toList();
    }

    public AccountDto getActiveAccount(Long accountId) {
        return accountRepository.findByIdAndActiveTrue(accountId)
                .map(accountMapper::toDto)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
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

        return accountMapper.toDto(accountRepository.save(account));
    }

    @Transactional
    public AccountDto updateAccount(Long accountId, UpdateAccountRequest request) {
        if (request.type() != null) {
            validateAccountType(request.type());
        }

        FinancialAccount account = accountRepository.findByIdAndActiveTrue(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (request.name() != null) {
            account.setName(request.name().trim());
        }
        if (request.type() != null) {
            account.setType(request.type());
        }
        if (request.currency() != null) {
            account.setCurrency(request.currency());
        }

        return accountMapper.toDto(account);
    }

    private void validateAccountType(FinancialAccountType type) {
        if (!SUPPORTED_ACCOUNT_TYPES.contains(type)) {
            throw new UnsupportedAccountTypeException(type);
        }
    }
}
