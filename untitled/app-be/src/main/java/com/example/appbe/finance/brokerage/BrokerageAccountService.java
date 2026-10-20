package com.example.appbe.finance.brokerage;

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
public class BrokerageAccountService {
    private final BrokerageAccountRepository accounts;
    private final TransferRepository transfers;
    private final UserRepository users;
    public BrokerageAccountService(BrokerageAccountRepository accounts, TransferRepository transfers, UserRepository users) {
        this.accounts = accounts; this.transfers = transfers; this.users = users;
    }
    @Transactional
    public Long create(String name, String currency, InvestmentType type) {
        if (type != InvestmentType.ETF && type != InvestmentType.OTHER) {
            throw new InvestmentException("INVALID_BROKERAGE_TYPE", "Wybierz ETF lub inny rachunek maklerski.");
        }
        var owner = users.findForUpdate(1L).orElseThrow();
        return accounts.saveAndFlush(new BrokerageAccount(owner, name.trim(), currency, type)).getId();
    }
    public BrokerageAccount getOwned(Long id) {
        return accounts.findOwned(id, 1L).orElseThrow(() ->
                new InvestmentException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Nie znaleziono konta."));
    }
    public BigDecimal cashBalance(Long id) {
        getOwned(id);
        return transfers.sumCashBalance(id, 1L);
    }
    public InvestmentDto summary(Long id) { return toDto(getOwned(id)); }
    public List<InvestmentDto> summaries() { return accounts.findOwned(1L).stream().map(this::toDto).toList(); }
    private InvestmentDto toDto(BrokerageAccount account) {
        return InvestmentDto.cashAccount(account.getId(), account.getName(), account.getType().name(), account.getCurrency(),
                transfers.sumCashBalance(account.getId(), 1L), LocalDate.now(ZoneId.of("Europe/Warsaw")));
    }
    @Transactional
    public void deleteProfile(Long id) {
        accounts.delete(getOwned(id));
        accounts.flush();
    }
}
