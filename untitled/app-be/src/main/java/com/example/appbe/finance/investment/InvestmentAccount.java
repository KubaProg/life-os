package com.example.appbe.finance.investment;

import com.example.appbe.user.User;
import com.example.appbe.finance.account.Account;
import com.example.appbe.finance.account.AccountKind;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** One investment contract per account in this first slice; ordinary accounts stay separate. */
@Entity
@Table(name = "investment_accounts")
public class InvestmentAccount {
    @Id
    private Long id;
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "id", nullable = false)
    private Account account;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InvestmentType type;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal principal;
    @Embedded
    private DepositTerms depositTerms;
    @Column(name = "funded_on")
    private LocalDate fundedOn;
    @Column(name = "settled_on")
    private LocalDate settledOn;
    protected InvestmentAccount() {}
    public InvestmentAccount(User user, String name, String currency, BigDecimal principal, DepositTerms terms) {
        this.account = new Account(user, name, currency, AccountKind.INVESTMENT);
        this.type = InvestmentType.DEPOSIT;
        this.principal = principal; this.depositTerms = terms;
    }
    public Long getId() { return id; }
    public Account getAccount() { return account; }
    public String getName() { return account.getName(); }
    public InvestmentType getType() { return type; }
    public String getCurrency() { return account.getCurrency(); }
    public BigDecimal getPrincipal() { return principal; }
    public DepositTerms getDepositTerms() { return depositTerms; }
    public LocalDate getFundedOn() { return fundedOn; }
    public void fund(LocalDate date) { fundedOn = date; }
    public LocalDate getSettledOn() { return settledOn; }
    public void settle(LocalDate date) { settledOn = date; }
}
