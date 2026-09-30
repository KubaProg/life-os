package com.example.appbe.finance.account;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountDto> getActiveAccounts() {
        return accountService.getActiveAccounts();
    }

    @GetMapping("/{accountId}")
    public AccountDto getActiveAccount(@PathVariable Long accountId) {
        return accountService.getActiveAccount(accountId);
    }
}
