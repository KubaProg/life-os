package com.example.appbe.finance.account;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDto createAccount(@Valid @RequestBody AccountRequest request) {
        return accountService.createAccount(request);
    }

    @PatchMapping("/{accountId}")
    public AccountDto updateAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody UpdateAccountRequest request
    ) {
        return accountService.updateAccount(accountId, request);
    }
}
