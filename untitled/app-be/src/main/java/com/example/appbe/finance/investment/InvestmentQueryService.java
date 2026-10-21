package com.example.appbe.finance.investment;

import com.example.appbe.finance.account.AccountKind;
import com.example.appbe.finance.account.AccountRepository;
import com.example.appbe.finance.bond.BondAccountService;
import com.example.appbe.finance.bond.BondService;
import com.example.appbe.finance.brokerage.BrokerageAccountService;
import com.example.appbe.finance.overview.InvestmentProfitDto;
import com.example.appbe.finance.overview.MoneyTotalDto;
import com.example.appbe.user.User;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read facade for the dashboard; product state and rules belong to dedicated services. */
@Service
@Transactional
public class InvestmentQueryService {
    private final DepositQueryService deposits;
    private final BondAccountService bonds;
    private final BrokerageAccountService brokerage;
    private final AccountRepository accounts;
    private final BondService bondPositions;
    public InvestmentQueryService(DepositQueryService deposits, BondAccountService bonds,
            BrokerageAccountService brokerage, AccountRepository accounts, BondService bondPositions) {
        this.deposits = deposits; this.bonds = bonds; this.brokerage = brokerage; this.accounts = accounts;
        this.bondPositions = bondPositions;
    }
    public User lockOwner() { return deposits.lockOwner(); }
    public AccountKind kind(Long id) {
        return accounts.findByIdAndUserIdAndActiveTrue(id, 1L).filter(account -> account.getKind() != AccountKind.ORDINARY)
                .orElseThrow(() -> new InvestmentException(HttpStatus.NOT_FOUND, "INVESTMENT_NOT_FOUND", "Nie znaleziono konta inwestycyjnego."))
                .getKind();
    }
    public record Snapshot(List<InvestmentDto> investments, List<MoneyTotalDto> totals,
            InvestmentProfitDto profit, List<MoneyTotalDto> paidIncome) {}
    public Snapshot snapshot(YearMonth month) {
        var depositSnapshot = deposits.snapshot(month);
        List<InvestmentDto> result = new ArrayList<>(depositSnapshot.investments());
        result.addAll(bonds.summaries());
        result.addAll(brokerage.summaries());
        result.sort(Comparator.comparing(InvestmentDto::id));
        Map<String, BigDecimal> totals = new TreeMap<>();
        result.forEach(account -> totals.merge(account.currency(), account.currentValue(), BigDecimal::add));
        var bondMonth = bondPositions.monthly(month);
        List<MoneyTotalDto> profit = mergePln(depositSnapshot.profit().totals(), bondMonth.profit(), bondMonth.hasInvestments());
        return new Snapshot(result, totals.entrySet().stream().map(entry -> new MoneyTotalDto(entry.getKey(), entry.getValue())).toList(),
                new InvestmentProfitDto(month, false, profit), mergePln(depositSnapshot.paidIncome(), bondMonth.paidIncome(), false));
    }
    private List<MoneyTotalDto> mergePln(List<MoneyTotalDto> values, BigDecimal pln, boolean includeZero) {
        Map<String, BigDecimal> result = new TreeMap<>();
        values.forEach(value -> result.merge(value.currency(), value.amount(), BigDecimal::add));
        if (includeZero || pln.signum() != 0) result.merge("PLN", pln, BigDecimal::add);
        return result.entrySet().stream().map(e -> new MoneyTotalDto(e.getKey(), e.getValue())).toList();
    }
    public InvestmentDetailsDto details(Long id) {
        return switch (kind(id)) {
            case DEPOSIT -> deposits.details(id);
            case BOND -> new InvestmentDetailsDto(bonds.summary(id), List.of(), List.of());
            case BROKERAGE -> new InvestmentDetailsDto(brokerage.summary(id), List.of(), List.of());
            case ORDINARY -> throw new InvestmentException("INVALID_ACCOUNT_KIND", "To nie jest konto inwestycyjne.");
        };
    }
}
