package com.example.appbe.finance.bond;

import com.example.appbe.finance.investment.InvestmentException;
import com.example.appbe.finance.transfer.TransferRepository;
import com.example.appbe.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.appbe.finance.bond.BondOperation.Type.*;
import static com.example.appbe.finance.bond.BondOperation.Status.*;

@Service
@Transactional
public class BondService {
    private final BondAccountRepository accounts;
    private final BondIssueRepository issues;
    private final BondRepository bonds;
    private final BondOperationRepository operations;
    private final TransferRepository transfers;
    private final UserRepository users;

    public BondService(BondAccountRepository accounts, BondIssueRepository issues, BondRepository bonds,
            BondOperationRepository operations, TransferRepository transfers, UserRepository users) {
        this.accounts = accounts; this.issues = issues; this.bonds = bonds;
        this.operations = operations; this.transfers = transfers; this.users = users;
    }
    public static LocalDate today() { return LocalDate.now(ZoneId.of("Europe/Warsaw")); }
    private void lock() { users.findForUpdate(1L).orElseThrow(); }
    private BondAccount owned(Long id) {
        return accounts.findOwned(id, 1L).orElseThrow(() ->
                new InvestmentException(HttpStatus.NOT_FOUND, "BOND_ACCOUNT_NOT_FOUND", "Nie znaleziono konta obligacyjnego."));
    }
    private Bond ownedBond(Long accountId, Long bondId) {
        owned(accountId);
        return bonds.findByIdAndAccountId(bondId, accountId).orElseThrow(() ->
                new InvestmentException(HttpStatus.NOT_FOUND, "BOND_NOT_FOUND", "Nie znaleziono partii obligacji."));
    }
    public List<BondDtos.Issue> issues() { return issues.findAllByOrderBySaleFromDesc().stream().map(BondDtos.Issue::from).toList(); }

    public BondDtos.PurchaseQuote purchaseQuote(Long accountId, Long issueId, int quantity, LocalDate date) {
        lock();
        var account = owned(accountId);
        var issue = issues.findById(issueId).orElseThrow(() -> new InvestmentException("BOND_ISSUE_NOT_FOUND", "Nie znaleziono emisji TOS."));
        if (quantity < 1 || quantity > 1000000 || !issue.getCurrency().equals(account.getCurrency())
                || date.isAfter(today()) || date.isBefore(issue.getSaleFrom()) || date.isAfter(issue.getSaleUntil()))
            throw new InvestmentException("INVALID_BOND_PURCHASE", "Wybierz poprawną liczbę sztuk, konto PLN i datę w okresie sprzedaży emisji.");
        BigDecimal cost = issue.getNominal().multiply(BigDecimal.valueOf(quantity));
        BigDecimal cash = availableToSpend(accountId, date);
        return new BondDtos.PurchaseQuote(issueId, quantity, date, date.plusYears(issue.getDurationYears()), cost, cash, cash.compareTo(cost) >= 0);
    }

    public Long purchase(Long accountId, BondDtos.PurchaseRequest request) {
        lock();
        BondAccount account = owned(accountId);
        BondOperation previous = operations.findByRequestId(request.requestId().toString()).orElse(null);
        if (previous != null) {
            if (previous.getType() != PURCHASE || !previous.getBond().getOriginalAccountId().equals(accountId)
                    || !previous.getBond().getIssue().getId().equals(request.issueId())
                    || previous.getQuantity() != request.quantity() || !previous.getInstructionDate().equals(request.date())) conflict();
            return previous.getBond().getId();
        }
        BondIssue issue = issues.findById(request.issueId()).orElseThrow(() ->
                new InvestmentException("BOND_ISSUE_NOT_FOUND", "Nie znaleziono emisji TOS."));
        if (!issue.getCurrency().equals(account.getCurrency()))
            throw new InvestmentException("BOND_CURRENCY_MISMATCH", "Obligacje TOS można kupić wyłącznie na koncie w PLN.");
        if (request.date().isAfter(today()) || request.date().isBefore(issue.getSaleFrom()) || request.date().isAfter(issue.getSaleUntil()))
            throw new InvestmentException("INVALID_BOND_PURCHASE_DATE", "Data zakupu musi przypadać w okresie sprzedaży wybranej emisji i nie może być przyszła.");
        BigDecimal cost = issue.getNominal().multiply(BigDecimal.valueOf(request.quantity()));
        if (availableToSpend(accountId, request.date()).compareTo(cost) < 0)
            throw new InvestmentException("INSUFFICIENT_BOND_CASH", "Brak gotówki na koncie obligacyjnym w tym dniu. Najpierw dopłać środki.");
        Bond bond = bonds.saveAndFlush(new Bond(account, issue, request.quantity(), request.date()));
        operations.saveAndFlush(new BondOperation(bond, "purchase:" + request.requestId(), request.requestId().toString(),
                PURCHASE, SETTLED, request.date(), request.date(), request.date(), request.quantity(), cost,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, cost.negate()));
        settleDue();
        return bond.getId();
    }

    public BondDtos.RedemptionQuote quote(Long accountId, Long bondId, int quantity, LocalDate date) {
        lock(); settleDue();
        Bond bond = ownedBond(accountId, bondId);
        validateRedemption(bond, quantity, date);
        LocalDate fifth = PolishBusinessCalendar.addBusinessDays(date, 5);
        LocalDate payment = PolishBusinessCalendar.onOrAfter(fifth.plusDays(1));
        // Annex 2 uses an exclusive end; interest includes the fifth business day.
        var value = BondCalculator.calculate(bond, quantity, fifth.plusDays(1), true);
        return new BondDtos.RedemptionQuote(bondId, quantity, date, payment, value.principal(), value.grossInterest(),
                value.fee(), value.tax(), value.netValue());
    }

    public void redeem(Long accountId, Long bondId, BondDtos.RedemptionRequest request) {
        lock();
        owned(accountId);
        BondOperation previous = operations.findByRequestId(request.requestId().toString()).orElse(null);
        if (previous != null) {
            if (previous.getType() != EARLY_REDEMPTION || !previous.getBond().getId().equals(bondId)
                    || !previous.getBond().getOriginalAccountId().equals(accountId)
                    || previous.getQuantity() != request.quantity() || !previous.getInstructionDate().equals(request.date())) conflict();
            return;
        }
        BondDtos.RedemptionQuote quote = quote(accountId, bondId, request.quantity(), request.date());
        Bond bond = ownedBond(accountId, bondId);
        bond.reserve(request.quantity());
        LocalDate accrualUntil = PolishBusinessCalendar.addBusinessDays(request.date(), 5).plusDays(1);
        operations.saveAndFlush(new BondOperation(bond, "early:" + request.requestId(), request.requestId().toString(),
                EARLY_REDEMPTION, PENDING, request.date(), accrualUntil, quote.settlementDate(), request.quantity(),
                quote.principal(), quote.grossInterest(), quote.fee(), quote.tax(), quote.netCash()));
        settleDue();
    }
    private void validateRedemption(Bond bond, int quantity, LocalDate date) {
        if (quantity < 1 || quantity > bond.getAvailableQuantity())
            throw new InvestmentException("INVALID_BOND_QUANTITY", "Liczba sztuk przekracza dostępną liczbę obligacji (po odjęciu dyspozycji w toku).");
        // MVP records new instructions today; backdating them would rewrite settled financial history.
        if (!date.equals(today()))
            throw new InvestmentException("INVALID_REDEMPTION_DATE", "Nową dyspozycję wykupu można złożyć z dzisiejszą datą.");
        if (date.isBefore(bond.getPurchaseDate().plusDays(7)) || date.isAfter(bond.getMaturityDate().minusDays(20)))
            throw new InvestmentException("BOND_REDEMPTION_WINDOW", "Wykup można zlecić po 7 dniach od zakupu i najpóźniej 20 dni przed zapadalnością.");
    }
    private void conflict() {
        throw new InvestmentException(HttpStatus.CONFLICT, "BOND_REQUEST_CONFLICT", "Ten identyfikator operacji został już użyty z innymi danymi. Otwórz formularz ponownie.");
    }

    /** Shared user lock + unique event keys serialize scheduler, reads and commands. */
    public void settleDue() {
        lock();
        LocalDate today = today();
        List<BondOperation> history = operations.findByBondUserIdOrderBySettlementDateAscIdAsc(1L);
        for (BondOperation op : history) {
            if (op.getType() == EARLY_REDEMPTION && op.getStatus() == PENDING && !op.getSettlementDate().isAfter(today)) {
                op.getBond().settle(op.getQuantity(), true);
                op.settle();
            }
        }
        for (Bond bond : bonds.findByUserIdOrderByIdAsc(1L)) {
            if (bond.getRemainingQuantity() == 0) continue;
            for (int year = bond.getCapitalizedYears() + 1; year < bond.getIssue().getDurationYears(); year++) {
                LocalDate date = bond.getPurchaseDate().plusYears(year);
                if (date.isAfter(today)) break;
                BigDecimal previousBase = bond.getCapitalizedUnitValue();
                BigDecimal base = BondCalculator.capitalized(bond.getIssue(), year);
                bond.capitalize(year, base);
                String key = "capitalization:" + bond.getId() + ":" + year;
                if (!operations.existsByEventKey(key)) {
                    // Catch-up must use the quantity held on the anniversary, not today's remainder.
                    int quantity = bond.getOriginalQuantity() - history.stream().filter(op ->
                            op.getBond().getId().equals(bond.getId()) && isRedemption(op) && op.getStatus() == SETTLED
                            && !op.getSettlementDate().isAfter(date)).mapToInt(BondOperation::getQuantity).sum();
                    if (quantity > 0) operations.save(new BondOperation(bond, key, null, CAPITALIZATION, SETTLED,
                            date, date, date, quantity, BigDecimal.ZERO, base.subtract(previousBase).multiply(BigDecimal.valueOf(quantity)),
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
                }
            }
            LocalDate payment = PolishBusinessCalendar.onOrAfter(bond.getMaturityDate());
            if (!payment.isAfter(today) && bond.getAvailableQuantity() > 0) {
                int quantity = bond.getAvailableQuantity();
                var value = BondCalculator.calculate(bond, quantity, bond.getMaturityDate(), false);
                String key = "maturity:" + bond.getId();
                if (!operations.existsByEventKey(key)) {
                    operations.save(new BondOperation(bond, key, null, MATURITY_REDEMPTION, SETTLED,
                            bond.getMaturityDate(), bond.getMaturityDate(), payment, quantity, value.principal(),
                            value.grossInterest(), value.fee(), value.tax(), value.netValue()));
                    bond.settle(quantity, false);
                }
            }
        }
        bonds.flush(); operations.flush();
    }
    private static boolean isRedemption(BondOperation op) { return op.getType() == EARLY_REDEMPTION || op.getType() == MATURITY_REDEMPTION; }

    public BigDecimal cashBalance(Long accountId) {
        owned(accountId); settleDue();
        return readCashBalance(accountId);
    }
    private BigDecimal readCashBalance(Long accountId) {
        return transfers.sumCashBalance(accountId, 1L).add(operations.findByBondAccountIdOrderBySettlementDateAscIdAsc(accountId)
                .stream().filter(op -> op.getStatus() == SETTLED).map(BondOperation::getNetCash).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
    /** Minimum cash from this date onward prevents backdated purchases overdrawing later entries. */
    public BigDecimal availableToSpend(Long accountId, LocalDate date) {
        owned(accountId); settleDue();
        NavigableMap<LocalDate, BigDecimal> events = new TreeMap<>();
        transfers.findByUserIdOrderByDateDescIdDesc(1L).forEach(transfer -> {
            var source = transfer.sourceReference(); var destination = transfer.destinationReference();
            if (source.id().equals(accountId)) events.merge(transfer.getDate(), transfer.getAmount().negate(), BigDecimal::add);
            if (destination.id().equals(accountId)) events.merge(transfer.getDate(), transfer.getAmount(), BigDecimal::add);
        });
        operations.findByBondAccountIdOrderBySettlementDateAscIdAsc(accountId).stream().filter(op -> op.getStatus() == SETTLED)
                .forEach(op -> events.merge(op.getSettlementDate(), op.getNetCash(), BigDecimal::add));
        BigDecimal balance = events.headMap(date, true).values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal minimum = balance;
        for (BigDecimal change : events.tailMap(date, false).values()) { balance = balance.add(change); minimum = minimum.min(balance); }
        return minimum;
    }
    public BondDtos.Details details(Long accountId) {
        owned(accountId); settleDue();
        var history = operations.findByBondAccountIdOrderBySettlementDateAscIdAsc(accountId);
        List<BondDtos.Position> positions = bonds.findByAccountIdOrderByIdDesc(accountId).stream()
                .map(bond -> position(bond, history)).toList();
        BigDecimal principal = BigDecimal.ZERO, value = BigDecimal.ZERO, gross = BigDecimal.ZERO, tax = BigDecimal.ZERO, realized = BigDecimal.ZERO;
        for (var p : positions) {
            principal = principal.add(p.principal()); value = value.add(p.currentValue());
            gross = gross.add(p.grossProfit()); tax = tax.add(p.estimatedTax()); realized = realized.add(p.realizedProfit());
        }
        return new BondDtos.Details(accountId, readCashBalance(accountId), principal, principal.add(gross), value,
                gross, tax, gross.subtract(tax), realized, positions, history.stream().map(BondDtos.Operation::from).toList());
    }
    private BondDtos.Position position(Bond bond, List<BondOperation> history) {
        var value = remainingValue(bond, history, today());
        BigDecimal realized = history.stream().filter(op -> op.getBond().getId().equals(bond.getId())
                && isRedemption(op) && op.getStatus() == SETTLED).map(BondOperation::netProfit).reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDate earliest = bond.getPurchaseDate().plusDays(7), latest = bond.getMaturityDate().minusDays(20);
        boolean canRedeem = bond.getAvailableQuantity() > 0 && !today().isBefore(earliest) && !today().isAfter(latest);
        return new BondDtos.Position(bond.getId(), bond.getIssue().getCode(), bond.getPurchaseDate(), bond.getMaturityDate(),
                bond.getOriginalQuantity(), bond.getRemainingQuantity(), bond.getPendingQuantity(), bond.getAvailableQuantity(),
                value.principal(), value.principal().add(value.grossInterest()), value.netValue(), value.grossInterest(), value.tax(),
                value.netProfit(), bond.getIssue().getAnnualRate(), realized, earliest, latest, canRedeem,
                bond.getRemainingQuantity() == 0 ? "CLOSED" : bond.getPendingQuantity() > 0 ? "REDEMPTION_PENDING" : "ACTIVE");
    }
    private BondCalculator.Value remainingValue(Bond bond, List<BondOperation> history, LocalDate date) {
        // Pending units stop accruing after the contractual fifth business day, even over a weekend.
        BigDecimal gross = BigDecimal.ZERO;
        int pending = 0;
        for (var op : history) {
            if (op.getBond().getId().equals(bond.getId()) && op.getType() == EARLY_REDEMPTION && op.getStatus() == PENDING) {
                pending += op.getQuantity();
                LocalDate end = date.isBefore(op.getAccrualUntil()) ? date : op.getAccrualUntil();
                gross = gross.add(BondCalculator.calculate(bond, op.getQuantity(), end, false).grossInterest());
            }
        }
        gross = gross.add(BondCalculator.calculate(bond, bond.getRemainingQuantity() - pending, date, false).grossInterest());
        BigDecimal principal = bond.getUnitPurchasePrice().multiply(BigDecimal.valueOf(bond.getRemainingQuantity()));
        BigDecimal tax = gross.multiply(bond.getIssue().getTaxRate().movePointLeft(2)).setScale(2, java.math.RoundingMode.CEILING);
        BigDecimal net = gross.subtract(tax);
        return new BondCalculator.Value(principal, gross, BigDecimal.ZERO, tax, net, principal.add(net));
    }
    public void requireEmptyAndDetach(Long accountId) {
        owned(accountId); settleDue();
        var positions = bonds.findByAccountIdOrderByIdDesc(accountId);
        if (positions.stream().anyMatch(b -> b.getRemainingQuantity() > 0))
            throw new InvestmentException("BOND_ACCOUNT_HAS_POSITIONS", "Najpierw wykup wszystkie obligacje i poczekaj na rozliczenie dyspozycji. Potem możesz usunąć konto.");
        positions.forEach(Bond::detachAccount);
        bonds.flush();
    }
    public void requireNoPositions(Long accountId) {
        settleDue();
        if (bonds.findByAccountIdOrderByIdDesc(accountId).stream().anyMatch(b -> b.getRemainingQuantity() > 0))
            throw new InvestmentException("BOND_ACCOUNT_HAS_POSITIONS", "Najpierw wykup wszystkie obligacje i poczekaj na rozliczenie dyspozycji. Potem możesz usunąć konto.");
    }
    public record Monthly(BigDecimal profit, BigDecimal paidIncome, boolean hasInvestments) {}
    public Monthly monthly(YearMonth month) {
        settleDue();
        var positions = bonds.findByUserIdOrderByIdAsc(1L);
        LocalDate until = month.plusMonths(1).atDay(1).isAfter(today()) ? today() : month.plusMonths(1).atDay(1);
        if (until.isBefore(month.atDay(1))) return new Monthly(BigDecimal.ZERO, BigDecimal.ZERO, !positions.isEmpty());
        BigDecimal change = BigDecimal.ZERO, paid = BigDecimal.ZERO;
        for (Bond bond : positions) {
            var history = operations.findByBondIdOrderBySettlementDateAscIdAsc(bond.getId());
            change = change.add(lifetimeProfit(bond, history, until, YearMonth.from(today()).equals(month))
                    .subtract(lifetimeProfit(bond, history, month.atDay(1), false)));
            paid = paid.add(history.stream().filter(op -> isRedemption(op) && op.getStatus() == SETTLED
                    && YearMonth.from(op.getSettlementDate()).equals(month)).map(BondOperation::netProfit).reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return new Monthly(change, paid, !positions.isEmpty());
    }
    private BigDecimal lifetimeProfit(Bond bond, List<BondOperation> history, LocalDate date, boolean includePaymentsOnDate) {
        if (date.isBefore(bond.getPurchaseDate())) return BigDecimal.ZERO;
        int quantity = bond.getOriginalQuantity();
        BigDecimal realized = BigDecimal.ZERO, pendingGross = BigDecimal.ZERO;
        for (var op : history) {
            if (isRedemption(op) && op.getStatus() == SETTLED && (op.getSettlementDate().isBefore(date)
                    || (includePaymentsOnDate && op.getSettlementDate().equals(date)))) {
                quantity -= op.getQuantity(); realized = realized.add(op.netProfit());
            } else if (op.getType() == EARLY_REDEMPTION && !op.getInstructionDate().isAfter(date)) {
                quantity -= op.getQuantity();
                LocalDate end = date.isBefore(op.getAccrualUntil()) ? date : op.getAccrualUntil();
                pendingGross = pendingGross.add(BondCalculator.calculate(bond, op.getQuantity(), end, false).grossInterest());
            }
        }
        BigDecimal gross = BondCalculator.calculate(bond, quantity, date, false).grossInterest().add(pendingGross);
        BigDecimal tax = gross.multiply(bond.getIssue().getTaxRate().movePointLeft(2)).setScale(2, java.math.RoundingMode.CEILING);
        return realized.add(gross.subtract(tax));
    }
}
