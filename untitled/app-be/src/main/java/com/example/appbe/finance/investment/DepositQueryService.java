package com.example.appbe.finance.investment;

import com.example.appbe.finance.overview.InvestmentProfitDto;
import com.example.appbe.finance.overview.MoneyTotalDto;
import com.example.appbe.user.User;
import com.example.appbe.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DepositQueryService {
    private static final Long USER_ID = 1L;
    private static final ZoneId ZONE = ZoneId.of("Europe/Warsaw");
    private final UserRepository users;
    private final DepositAccountRepository accounts;
    private final InvestmentValuationRepository valuations;
    private final InvestmentOperationRepository operations;

    public DepositQueryService(UserRepository users, DepositAccountRepository accounts,
            InvestmentValuationRepository valuations, InvestmentOperationRepository operations) {
        this.users = users; this.accounts = accounts;
        this.valuations = valuations; this.operations = operations;
    }

    // One local user's lock protects idempotent history writes and investment cash operations.
    public User lockOwner() {
        User user = users.findForUpdate(USER_ID).orElse(null);
        if (user == null) throw new InvestmentException(HttpStatus.NOT_FOUND, "LOCAL_USER_NOT_FOUND", "Brak lokalnego użytkownika.");
        return user;
    }

    public DepositAccount getOwned(Long id) {
        return accounts.findByIdAndUserId(id, USER_ID).orElseThrow(() ->
                new InvestmentException(HttpStatus.NOT_FOUND, "INVESTMENT_NOT_FOUND", "Nie znaleziono inwestycji."));
    }

    public record Snapshot(List<InvestmentDto> investments, List<MoneyTotalDto> totals,
                           InvestmentProfitDto profit, List<MoneyTotalDto> paidIncome) {}

    public Snapshot snapshot(YearMonth month) {
        lockOwner();
        LocalDate today = LocalDate.now(ZONE);
        List<InvestmentDto> result = new ArrayList<>();
        Map<String, BigDecimal> totalValues = new TreeMap<>();
        Map<String, BigDecimal> monthlyProfit = new TreeMap<>();
        for (DepositAccount account : accounts.findByUserIdOrderByIdAsc(USER_ID)) {
            NavigableMap<LocalDate, InvestmentValuation> history = materialize(account, today);
            InvestmentDto dto = toDto(account, history.isEmpty() ? null : history.lastEntry().getValue(), today);
            result.add(dto);
            totalValues.merge(account.getCurrency(), dto.currentValue(), BigDecimal::add);
            if (history.isEmpty()) continue;
            LocalDate from = boundary(account, month.atDay(1));
            LocalDate until = boundary(account, month.plusMonths(1).atDay(1).isAfter(today)
                    ? today : month.plusMonths(1).atDay(1));
            BigDecimal profit = until.isBefore(from) ? BigDecimal.ZERO
                    : history.get(until).getNetProfit().subtract(history.get(from).getNetProfit());
            monthlyProfit.merge(account.getCurrency(), profit, BigDecimal::add);
        }
        Map<String, BigDecimal> paidIncome = new TreeMap<>();
        operations.findByInvestmentUserIdAndTypeAndOperationDateBetween(USER_ID, "INTEREST_PAYOUT",
                month.atDay(1), month.atEndOfMonth()).forEach(operation ->
                paidIncome.merge(operation.investmentCurrency(), operation.getAmount(), BigDecimal::add));
        return new Snapshot(result, moneyTotals(totalValues), new InvestmentProfitDto(month, false, moneyTotals(monthlyProfit)),
                moneyTotals(paidIncome));
    }

    public InvestmentDetailsDto details(Long id) {
        lockOwner();
        DepositAccount account = getOwned(id);
        LocalDate today = LocalDate.now(ZONE);
        NavigableMap<LocalDate, InvestmentValuation> history = materialize(account, today);
        return new InvestmentDetailsDto(toDto(account, history.isEmpty() ? null : history.lastEntry().getValue(), today),
                history.values().stream().map(InvestmentDetailsDto.ValuationDto::from).toList(),
                operations.findByInvestmentIdOrderByOperationDateAscIdAsc(id).stream().map(InvestmentOperationDto::from).toList());
    }

    private NavigableMap<LocalDate, InvestmentValuation> materialize(DepositAccount account, LocalDate today) {
        NavigableMap<LocalDate, InvestmentValuation> history = new TreeMap<>();
        if (account.getFundedOn() == null) return history;
        valuations.findByInvestmentIdOrderByValuationDateAsc(account.getId())
                .forEach(value -> history.put(value.getValuationDate(), value));
        LocalDate lastDate = boundary(account, today);
        for (LocalDate date = account.getDepositTerms().getStartDate(); !date.isAfter(lastDate); date = date.plusDays(1)) {
            if (!history.containsKey(date)) {
                InvestmentValuation value = new InvestmentValuation(account, date, DepositCalculator.calculate(account, date));
                valuations.save(value);
                history.put(date, value);
            }
        }
        return history;
    }

    private LocalDate boundary(DepositAccount account, LocalDate date) {
        DepositTerms terms = account.getDepositTerms();
        if (date.isBefore(terms.getStartDate())) return terms.getStartDate();
        if (date.isAfter(terms.getMaturityDate())) return terms.getMaturityDate();
        return date;
    }

    private InvestmentDto toDto(DepositAccount account, InvestmentValuation value, LocalDate today) {
        DepositTerms terms = account.getDepositTerms();
        String status = terms == null ? "DRAFT" : account.getFundedOn() == null ? "AWAITING_FUNDING" : account.getSettledOn() != null ? "SETTLED"
                : today.isBefore(terms.getMaturityDate()) ? "ACTIVE" : "MATURED";
        BigDecimal zero = BigDecimal.ZERO;
        return new InvestmentDto(account.getId(), account.getName(), "DEPOSIT", account.getCurrency(),
                account.getPrincipal(), value == null || account.getSettledOn() != null ? zero : value.getCurrentValue(),
                value == null ? zero : value.getGrossProfit(), value == null ? zero : value.getEstimatedTax(),
                value == null ? zero : value.getNetProfit(), terms == null ? null : terms.getAnnualRate(), terms == null ? null : terms.getTaxRate(),
                terms == null ? null : terms.getCalculationRule(), terms == null ? null : terms.getCapitalization(),
                terms == null ? null : terms.getStartDate(), terms == null ? null : terms.getMaturityDate(),
                value == null ? today : value.getValuationDate(), status, account.getRefundablePrincipal());
    }

    private List<MoneyTotalDto> moneyTotals(Map<String, BigDecimal> values) {
        return values.entrySet().stream().map(entry -> new MoneyTotalDto(entry.getKey(), entry.getValue())).toList();
    }
}
