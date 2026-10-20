package com.example.appbe.finance.bond;

import com.example.appbe.finance.investment.*;
import com.example.appbe.finance.transfer.TransferRepository;
import com.example.appbe.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BondAccountService {
    private final BondAccountRepository accounts;
    private final TransferRepository transfers;
    private final UserRepository users;
    public BondAccountService(BondAccountRepository accounts, TransferRepository transfers, UserRepository users) {
        this.accounts = accounts; this.transfers = transfers; this.users = users;
    }
    @Transactional
    public Long create(String name, String currency) {
        var owner = users.findForUpdate(1L).orElseThrow();
        return accounts.saveAndFlush(new BondAccount(owner, name.trim(), currency)).getId();
    }
    public BondAccount getOwned(Long id) {
        return accounts.findOwned(id, 1L).orElseThrow(() ->
                new InvestmentException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Nie znaleziono konta."));
    }
    public BigDecimal cashBalance(Long id) {
        getOwned(id);
        return transfers.sumCashBalance(id, 1L);
    }
    public InvestmentDto summary(Long id) { return toDto(getOwned(id)); }
    public List<InvestmentDto> summaries() { return accounts.findOwned(1L).stream().map(this::toDto).toList(); }
    private InvestmentDto toDto(BondAccount account) {
        return InvestmentDto.cashAccount(account.getId(), account.getName(), "BOND", account.getCurrency(),
                transfers.sumCashBalance(account.getId(), 1L), LocalDate.now(ZoneId.of("Europe/Warsaw")));
    }
    @Transactional
    public void deleteProfile(Long id) {
        accounts.delete(getOwned(id));
        accounts.flush();
    }
}
