package com.example.appbe.finance.investment;

import com.example.appbe.finance.account.FinancialAccount;
import com.example.appbe.finance.account.AccountKind;
import com.example.appbe.finance.account.FinancialAccountRepository;
import com.example.appbe.finance.transfer.*;
import com.example.appbe.finance.overview.FinanceOverviewService;
import com.example.appbe.user.User;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvestmentService {
    private final InvestmentQueryService queries;
    private final InvestmentAccountRepository investments;
    private final TransferService transfers;
    private final TransferRepository transferRepository;
    private final FinanceOverviewService overview;
    private final FinancialAccountRepository cashAccounts;
    public InvestmentService(InvestmentQueryService queries, InvestmentAccountRepository investments, TransferService transfers, FinancialAccountRepository cashAccounts, TransferRepository transferRepository, FinanceOverviewService overview) {
        this.queries = queries; this.investments = investments; this.transfers = transfers; this.cashAccounts = cashAccounts; this.transferRepository = transferRepository; this.overview = overview;
    }
    @Transactional
    public InvestmentMutationResponse createDeposit(DepositRequest request, YearMonth month) {
        User user = queries.lockOwner();
        MoneyTransfer prior = transferRepository.findByRequestIdAndUserId(request.requestId().toString(), 1L).orElse(null);
        if (prior != null) {
            InvestmentAccount existing = prior.getDestinationAccount().getKind() == AccountKind.INVESTMENT
                    ? investments.findByIdAndUserId(prior.getDestinationAccount().getId(), 1L).orElse(null) : null;
            DepositTerms terms = existing == null ? null : existing.getDepositTerms();
            if (existing == null || terms == null || prior.getSourceAccount().getKind() != AccountKind.ORDINARY ||
                    !prior.getSourceAccount().getId().equals(request.sourceAccountId()) ||
                    !existing.getName().equals(request.name().trim()) || existing.getPrincipal().compareTo(request.principal()) != 0 ||
                    !terms.getStartDate().equals(request.startDate()) || !terms.getMaturityDate().equals(request.maturityDate()) ||
                    terms.getAnnualRate().compareTo(request.annualRate()) != 0 || terms.getTaxRate().compareTo(request.taxRate()) != 0) {
                throw new InvestmentException("TRANSFER_REQUEST_CONFLICT", "Ta próba utworzenia lokaty została już zapisana z innymi danymi. Otwórz nowy formularz.");
            }
            return new InvestmentMutationResponse(existing.getId(), overview.getOverview(month));
        }
        FinancialAccount source = cashAccounts.findByIdAndUserIdAndActiveTrue(request.sourceAccountId(), 1L)
                .orElseThrow(() -> new InvestmentException("ACCOUNT_NOT_FOUND", "Konto źródłowe nie jest aktywne."));
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Warsaw"));
        if (request.startDate().isAfter(today) || request.startDate().isBefore(today.minusYears(10))) {
            throw new InvestmentException("INVALID_DEPOSIT_START", "Początek lokaty musi przypadać w ciągu ostatnich 10 lat, najpóźniej dzisiaj.");
        }
        if (!request.maturityDate().isAfter(request.startDate()) || request.maturityDate().isAfter(request.startDate().plusYears(10))) {
            throw new InvestmentException("INVALID_DEPOSIT_TERM", "Termin lokaty musi wynosić od 1 dnia do 10 lat.");
        }
        DepositCalculator.Value maximumValue = DepositCalculator.calculate(request.principal(), request.annualRate(), request.taxRate(),
                request.startDate(), request.maturityDate(), request.maturityDate());
        if (maximumValue.currentValue().precision() - maximumValue.currentValue().scale() > 15 ||
                maximumValue.grossProfit().precision() - maximumValue.grossProfit().scale() > 15) {
            throw new InvestmentException("DEPOSIT_AMOUNT_TOO_LARGE", "Kwota wraz z odsetkami przekracza obsługiwany zakres.");
        }
        InvestmentAccount investment = investments.saveAndFlush(new InvestmentAccount(user, request.name().trim(), source.getCurrency(),
                request.principal(), new DepositTerms(request.annualRate(), request.taxRate(), request.startDate(), request.maturityDate())));
        // Both the account and its initial funding commit together, or neither is saved.
        TransferResponse funding = transfers.transfer(new TransferRequest(request.requestId(),
                source.getId(), investment.getId(),
                request.principal(), request.startDate()), month);
        return new InvestmentMutationResponse(investment.getId(), funding.overview());
    }
}
