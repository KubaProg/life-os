package com.example.appbe.finance.investment;

import com.example.appbe.user.User;
import com.example.appbe.finance.account.Account;
import com.example.appbe.finance.account.AccountKind;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** A single deposit contract, with the shared identity owned by Account. */
@Entity
@Table(name = "deposit_accounts")
public class DepositAccount {
    @Id
    private Long id;
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "id", nullable = false)
    private Account account;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal principal;
    @Embedded
    private DepositTerms depositTerms;
    @Column(name = "funded_on")
    private LocalDate fundedOn;
    @Column(name = "settled_on")
    private LocalDate settledOn;
    protected DepositAccount() {}
    public DepositAccount(User user, String name, String currency) {
        this.account = new Account(user, name, currency, AccountKind.DEPOSIT);
        this.principal = BigDecimal.ZERO;
    }
    public DepositAccount(User user, String name, String currency, BigDecimal principal, DepositTerms terms) {
        this.account = new Account(user, name, currency, AccountKind.DEPOSIT);
        this.principal = principal; this.depositTerms = terms;
    }
    public Long getId() { return id; }
    public Account getAccount() { return account; }
    public String getName() { return account.getName(); }
    public String getCurrency() { return account.getCurrency(); }
    public BigDecimal getPrincipal() { return principal; }
    public DepositTerms getDepositTerms() { return depositTerms; }
    public void configureDeposit(BigDecimal principal, DepositTerms terms) {
        this.principal = principal;
        this.depositTerms = terms;
    }
    public LocalDate getFundedOn() { return fundedOn; }
    public void fund(LocalDate date) { fundedOn = date; }
    public LocalDate getSettledOn() { return settledOn; }
    public void settle(LocalDate date) { settledOn = date; }
}
