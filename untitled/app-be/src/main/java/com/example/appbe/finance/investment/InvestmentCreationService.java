package com.example.appbe.finance.investment;

import com.example.appbe.finance.bond.BondAccountService;
import com.example.appbe.finance.brokerage.BrokerageAccountService;
import com.example.appbe.finance.overview.FinanceOverviewService;
import java.time.YearMonth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvestmentCreationService {
    private final DepositService deposits;
    private final BondAccountService bonds;
    private final BrokerageAccountService brokerage;
    private final FinanceOverviewService overview;
    public InvestmentCreationService(DepositService deposits, BondAccountService bonds,
            BrokerageAccountService brokerage, FinanceOverviewService overview) {
        this.deposits = deposits; this.bonds = bonds; this.brokerage = brokerage; this.overview = overview;
    }
    @Transactional
    public InvestmentMutationResponse create(InvestmentAccountRequest request, YearMonth month) {
        if (request.type() == InvestmentType.DEPOSIT) return deposits.createAccount(request, month);
        Long id = switch (request.type()) {
            case BOND -> bonds.create(request.name(), request.currency());
            case ETF, OTHER -> brokerage.create(request.name(), request.currency(), request.type());
            case DEPOSIT -> throw new IllegalStateException("Deposit creation was already handled");
        };
        return new InvestmentMutationResponse(id, overview.getOverview(month));
    }
}
