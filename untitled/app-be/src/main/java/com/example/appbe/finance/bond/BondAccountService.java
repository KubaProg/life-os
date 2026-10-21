package com.example.appbe.finance.bond;

import com.example.appbe.finance.investment.*;
import com.example.appbe.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BondAccountService {
    private final BondAccountRepository accounts;
    private final UserRepository users;
    private final BondService bonds;
    public BondAccountService(BondAccountRepository accounts, UserRepository users, BondService bonds) {
        this.accounts = accounts; this.users = users;
        this.bonds = bonds;
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
        return bonds.cashBalance(id);
    }
    public BigDecimal availableToSpend(Long id, LocalDate date) { return bonds.availableToSpend(id, date); }
    public void requireNoPositions(Long id) { bonds.requireNoPositions(id); }
    public InvestmentDto summary(Long id) { return toDto(getOwned(id)); }
    public List<InvestmentDto> summaries() { return accounts.findOwned(1L).stream().map(this::toDto).toList(); }
    private InvestmentDto toDto(BondAccount account) {
        var value = bonds.details(account.getId());
        return new InvestmentDto(account.getId(), account.getName(), "BOND", account.getCurrency(), value.principal(),
                value.availableCash().add(value.currentValue()), value.grossProfit(), value.estimatedTax(), value.netProfit(),
                null, null, null, null, null, null, LocalDate.now(ZoneId.of("Europe/Warsaw")), "READY", value.availableCash());
    }
    @Transactional
    public void deleteProfile(Long id) {
        bonds.requireEmptyAndDetach(id);
        accounts.delete(getOwned(id));
        accounts.flush();
    }
}
