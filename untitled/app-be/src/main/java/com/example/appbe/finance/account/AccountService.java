package com.example.appbe.finance.account;

import com.example.appbe.exception.AccountNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountService {

    private final FinancialAccountRepository accountRepository;

    public AccountService(FinancialAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<AccountDto> getActiveAccounts() {
        return accountRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toDto)
                .toList();
    }

    public AccountDto getActiveAccount(Long accountId) {
        return accountRepository.findByIdAndActiveTrue(accountId)
                .map(this::toDto)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private AccountDto toDto(FinancialAccount account) {
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
