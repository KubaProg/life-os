package com.example.appbe.finance.account;

import com.example.appbe.finance.investment.*;
import com.example.appbe.finance.bond.BondAccountService;
import com.example.appbe.finance.brokerage.BrokerageAccountService;
import com.example.appbe.finance.overview.FinanceOverviewService;
import com.example.appbe.finance.transaction.TransactionRepository;
import com.example.appbe.finance.transfer.TransferRepository;
import com.example.appbe.finance.transfer.TransferRequest;
import com.example.appbe.finance.transfer.TransferService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountDeletionService {
    private final AccountRepository accounts;
    private final FinancialAccountRepository financialAccounts;
    private final DepositAccountRepository investmentAccounts;
    private final DepositQueryService deposits;
    private final BondAccountService bonds;
    private final BrokerageAccountService brokerage;
    private final AccountBalanceService balances;
    private final InvestmentQueryService investments;
    private final TransactionRepository transactions;
    private final TransferRepository transferRepository;
    private final TransferService transfers;
    private final InvestmentOperationRepository operations;
    private final InvestmentValuationRepository valuations;
    private final FinanceOverviewService overview;

    public AccountDeletionService(AccountRepository accounts, FinancialAccountRepository financialAccounts,
            DepositAccountRepository investmentAccounts, AccountBalanceService balances, InvestmentQueryService investments,
            TransactionRepository transactions, TransferRepository transferRepository, TransferService transfers,
            InvestmentOperationRepository operations, InvestmentValuationRepository valuations, FinanceOverviewService overview,
            DepositQueryService deposits, BondAccountService bonds, BrokerageAccountService brokerage) {
        this.accounts = accounts; this.financialAccounts = financialAccounts; this.investmentAccounts = investmentAccounts;
        this.balances = balances; this.investments = investments; this.transactions = transactions;
        this.transferRepository = transferRepository; this.transfers = transfers; this.operations = operations;
        this.valuations = valuations; this.overview = overview;
        this.deposits = deposits; this.bonds = bonds; this.brokerage = brokerage;
    }

    @Transactional
    public AccountDeletedResponse delete(Long id, AccountKind expectedKind, DeleteAccountRequest request, YearMonth month) {
        investments.lockOwner();
        Account account = accounts.findByIdAndUserIdAndActiveTrue(id, 1L)
                .filter(value -> value.getKind() == expectedKind)
                .orElseThrow(() -> new InvestmentException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Nie znaleziono konta."));
        FinancialAccount financial = expectedKind == AccountKind.ORDINARY ? financialAccounts.findById(id).orElseThrow() : null;
        DepositAccount investment = expectedKind == AccountKind.DEPOSIT ? deposits.getOwned(id) : null;
        BigDecimal balance = financial != null ? balances.calculateCurrentBalance(financial)
                : investment != null ? investment.getRefundablePrincipal() : investments.details(id).investment().currentValue();
        if (balance.compareTo(request.expectedBalance()) != 0) {
            throw new InvestmentException(HttpStatus.CONFLICT, "ACCOUNT_BALANCE_CHANGED", "Saldo uległo zmianie. Odśwież szczegóły konta i ponów usuwanie.");
        }
        if (balance.signum() < 0) {
            throw new InvestmentException("ACCOUNT_NEGATIVE_BALANCE", "Przed usunięciem konta ureguluj jego ujemne saldo.");
        }
        if (balance.signum() > 0) {
            if (request.destinationAccountId() == null || id.equals(request.destinationAccountId())) {
                throw new InvestmentException("DESTINATION_ACCOUNT_REQUIRED", "Wybierz inne konto, na które przenieść pozostałe środki.");
            }
            FinancialAccount destination = financialAccounts.findByIdAndUserIdAndActiveTrue(request.destinationAccountId(), 1L)
                    .orElseThrow(() -> new InvestmentException("ACCOUNT_NOT_FOUND", "Konto docelowe musi być aktywnym zwykłym kontem."));
            if (!destination.getCurrency().equals(account.getCurrency())) {
                throw new InvestmentException("TRANSFER_CURRENCY_MISMATCH", "Konto docelowe musi mieć tę samą walutę.");
            }
            TransferRequest refund = new TransferRequest(UUID.randomUUID(), id, destination.getId(), balance,
                    LocalDate.now(ZoneId.of("Europe/Warsaw")));
            if (investment != null) {
                transfers.returnDepositPrincipal(refund, month);
            } else {
                transfers.transfer(refund, month);
            }
        }

        // Detach historical references before deleting either account row. Keep their readable snapshots.
        transferRepository.findByUserIdOrderByDateDescIdDesc(1L).forEach(transfer -> transfer.detachAccount(id));
        transferRepository.flush();
        if (financial != null) {
            transactions.findByUserIdAndFinancialAccountIdOrderByTransactionDateDescCreatedAtDesc(1L, id)
                    .forEach(transaction -> transaction.detachAccount());
            transactions.flush();
            financialAccounts.delete(financial);
            financialAccounts.flush();
        } else if (investment != null) {
            operations.findByInvestmentIdOrderByOperationDateAscIdAsc(id).forEach(operation -> operation.detachInvestment());
            operations.flush();
            valuations.deleteByInvestmentId(id);
            valuations.flush();
            investmentAccounts.delete(investment);
            investmentAccounts.flush();
        } else if (expectedKind == AccountKind.BOND) {
            bonds.deleteProfile(id);
        } else {
            brokerage.deleteProfile(id);
        }
        accounts.delete(account);
        accounts.flush();
        return new AccountDeletedResponse(id, overview.getOverview(month));
    }
}
