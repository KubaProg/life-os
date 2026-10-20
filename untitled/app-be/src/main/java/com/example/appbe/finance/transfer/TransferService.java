package com.example.appbe.finance.transfer;

import com.example.appbe.finance.account.*;
import com.example.appbe.finance.investment.*;
import com.example.appbe.finance.bond.BondAccountService;
import com.example.appbe.finance.brokerage.BrokerageAccountService;
import java.math.BigDecimal;
import com.example.appbe.finance.overview.FinanceOverviewService;
import com.example.appbe.user.User;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {
    private final AccountRepository accountRegistry;
    private final TransferRepository transfers;
    private final FinancialAccountRepository cashAccounts;
    private final AccountBalanceService balances;
    private final DepositQueryService investments;
    private final BondAccountService bonds;
    private final BrokerageAccountService brokerage;
    private final DepositAccountRepository investmentAccounts;
    private final InvestmentOperationRepository operations;
    private final FinanceOverviewService overview;

    public TransferService(AccountRepository accountRegistry, TransferRepository transfers, FinancialAccountRepository cashAccounts,
            AccountBalanceService balances, DepositQueryService investments, DepositAccountRepository investmentAccounts,
            BondAccountService bonds, BrokerageAccountService brokerage,
            InvestmentOperationRepository operations, FinanceOverviewService overview) {
        this.accountRegistry = accountRegistry;
        this.transfers = transfers; this.cashAccounts = cashAccounts; this.balances = balances;
        this.investments = investments; this.investmentAccounts = investmentAccounts;
        this.bonds = bonds; this.brokerage = brokerage;
        this.operations = operations; this.overview = overview;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request, YearMonth month) {
        return executeTransfer(request, month, false);
    }

    /** Cancellation returns only funded principal, forfeiting all unpaid interest. */
    @Transactional
    public TransferResponse returnDepositPrincipal(TransferRequest request, YearMonth month) {
        return executeTransfer(request, month, true);
    }

    private TransferResponse executeTransfer(TransferRequest request, YearMonth month, boolean cancelDeposit) {
        User user = investments.lockOwner();
        MoneyTransfer existing = transfers.findByRequestIdAndUserId(request.requestId().toString(), 1L).orElse(null);
        if (existing != null) {
            TransferDto dto = TransferDto.from(existing);
            if (!dto.source().id().equals(request.sourceAccountId()) || !dto.destination().id().equals(request.destinationAccountId()) ||
                    dto.amount().compareTo(request.amount()) != 0 || !dto.date().equals(request.date())) {
                throw new InvestmentException("TRANSFER_REQUEST_CONFLICT", "Ten identyfikator transferu został już użyty dla innych danych. Otwórz nowy formularz.");
            }
            return new TransferResponse(existing.getId(), overview.getOverview(month));
        }
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new InvestmentException("SAME_TRANSFER_ACCOUNT", "Źródło i cel muszą być różnymi rachunkami.");
        }
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Warsaw"));
        if (request.date().isAfter(today)) throw new InvestmentException("INVALID_TRANSFER_DATE", "Transfer nie może mieć przyszłej daty.");
        TransferSide source = resolve(request.sourceAccountId(), request.date());
        TransferSide destination = resolve(request.destinationAccountId(), request.date());
        DepositAccount sourceInvestment = source.investment();
        DepositAccount destinationInvestment = destination.investment();
        if (cancelDeposit && (sourceInvestment == null || destination.account().getKind() != AccountKind.ORDINARY)) {
            throw new InvestmentException("INVALID_DEPOSIT_CANCELLATION", "Zwrot kapitału wymaga lokaty i zwykłego konta docelowego.");
        }
        if (!source.account().getCurrency().equals(destination.account().getCurrency())) {
            throw new InvestmentException("TRANSFER_CURRENCY_MISMATCH", "Transfer wymaga tej samej waluty po obu stronach.");
        }
        if (source.availableCash() != null && source.availableCash().compareTo(request.amount()) < 0) {
            throw new InvestmentException("INSUFFICIENT_FUNDS", "Na koncie źródłowym brakuje środków.");
        }
        if ((sourceInvestment != null && destination.account().getKind() != AccountKind.ORDINARY) ||
                (destinationInvestment != null && source.account().getKind() != AccountKind.ORDINARY)) {
            throw new InvestmentException("UNSUPPORTED_TRANSFER", "W tym wariancie lokata może być finansowana i rozliczana przez zwykłe konto.");
        }
        if (destinationInvestment != null) {
            requireDeposit(destinationInvestment);
            if (destinationInvestment.getFundedOn() != null || request.amount().compareTo(destinationInvestment.getPrincipal()) != 0) {
                throw new InvestmentException("INVALID_DEPOSIT_FUNDING", "Lokata przyjmuje tylko jedną wpłatę, równą pełnemu kapitałowi z konfiguracji.");
            }
            if (!request.date().equals(destinationInvestment.getDepositTerms().getStartDate())) {
                throw new InvestmentException("INVALID_DEPOSIT_FUNDING_DATE", "Data wpłaty musi być zgodna z datą rozpoczęcia lokaty.");
            }
        }
        DepositCalculator.Value payout = null;
        if (sourceInvestment != null) {
            requireDeposit(sourceInvestment);
            if (sourceInvestment.getFundedOn() == null || sourceInvestment.getSettledOn() != null ||
                    (!cancelDeposit && request.date().isBefore(sourceInvestment.getDepositTerms().getMaturityDate()))) {
                throw new InvestmentException("DEPOSIT_NOT_WITHDRAWABLE", "Wypłata wymaga sfinansowanej, nierozliczonej lokaty po terminie zakończenia.");
            }
            payout = cancelDeposit ? null : DepositCalculator.calculate(sourceInvestment, request.date());
            BigDecimal expectedAmount = cancelDeposit ? sourceInvestment.getRefundablePrincipal() : payout.currentValue();
            if (request.amount().compareTo(expectedAmount) != 0) {
                if (cancelDeposit) {
                    throw new InvestmentException("INVALID_DEPOSIT_REFUND", "Przy usuwaniu lokaty zwracany jest wyłącznie cały kapitał, bez odsetek.");
                }
                throw new InvestmentException("INVALID_DEPOSIT_WITHDRAWAL", "Lokata jest rozliczana w całości: kapitał plus odsetki po podatku według konfiguracji.");
            }
        }
        MoneyTransfer transfer = transfers.saveAndFlush(new MoneyTransfer(user, request, source.account(), destination.account()));
        if (destinationInvestment != null) {
            destinationInvestment.fund(request.date());
            operations.save(new InvestmentOperation(destinationInvestment, transfer, "FUNDING", request.date(), request.amount()));
        }
        if (sourceInvestment != null) {
            sourceInvestment.settle(request.date());
            operations.save(new InvestmentOperation(sourceInvestment, transfer,
                    cancelDeposit ? "DEPOSIT_CANCELLATION" : "PRINCIPAL_RETURN", request.date(), sourceInvestment.getPrincipal()));
            if (!cancelDeposit) {
                operations.save(new InvestmentOperation(sourceInvestment, transfer, "INTEREST_PAYOUT", request.date(), payout.netProfit()));
            }
        }
        investmentAccounts.flush();
        operations.flush();
        return new TransferResponse(transfer.getId(), overview.getOverview(month));
    }

    private record TransferSide(Account account, DepositAccount investment, BigDecimal availableCash) {}

    private TransferSide resolve(Long id, LocalDate date) {
        Account account = accountRegistry.findByIdAndUserIdAndActiveTrue(id, 1L)
                .orElseThrow(() -> new InvestmentException("ACCOUNT_NOT_FOUND", "Rachunek nie istnieje lub jest nieaktywny."));
        // Storage is generic. Each family still needs an explicit policy for its money movements.
        return switch (account.getKind()) {
            case ORDINARY -> {
                FinancialAccount ordinary = cashAccounts.findById(id)
                        .orElseThrow(() -> new InvestmentException("ACCOUNT_PROFILE_MISSING", "Brak profilu zwykłego konta."));
                if (date.isBefore(ordinary.getBalanceStartDate())) {
                    throw new InvestmentException("INVALID_TRANSFER_DATE", "Data transferu nie może poprzedzać salda początkowego żadnego z kont.");
                }
                yield new TransferSide(account, null, balances.calculateCurrentBalance(ordinary));
            }
            case DEPOSIT -> new TransferSide(account, investments.getOwned(id), null);
            case BOND -> new TransferSide(account, null, bonds.cashBalance(id));
            case BROKERAGE -> new TransferSide(account, null, brokerage.cashBalance(id));
        };
    }
    private void requireDeposit(DepositAccount investment) {
        if (investment.getDepositTerms() == null) {
            throw new InvestmentException("INVESTMENT_NOT_CONFIGURED", "Najpierw uzupełnij warunki lokaty w szczegółach konta.");
        }
    }
}
