package com.example.appbe.finance.brokerage;

import com.example.appbe.finance.account.Account;
import com.example.appbe.finance.account.AccountKind;
import com.example.appbe.finance.investment.InvestmentType;
import com.example.appbe.user.User;
import jakarta.persistence.*;

/** Cash belongs to the account; purchased securities will be separate positions. */
@Entity
@Table(name = "brokerage_accounts")
public class BrokerageAccount {
    @Id private Long id;
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "id", nullable = false)
    private Account account;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InvestmentType type;

    protected BrokerageAccount() {}
    public BrokerageAccount(User user, String name, String currency, InvestmentType type) {
        this.account = new Account(user, name, currency, AccountKind.BROKERAGE);
        this.type = type;
    }
    public Long getId() { return id; }
    public Account getAccount() { return account; }
    public String getName() { return account.getName(); }
    public String getCurrency() { return account.getCurrency(); }
    public InvestmentType getType() { return type; }
}
