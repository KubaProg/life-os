package com.example.appbe.finance.bond;

import com.example.appbe.finance.account.Account;
import com.example.appbe.finance.account.AccountKind;
import com.example.appbe.user.User;
import jakarta.persistence.*;

/** Cash belongs to the account; purchased securities will be separate positions. */
@Entity
@Table(name = "bond_accounts")
public class BondAccount {
    @Id private Long id;
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "id", nullable = false)
    private Account account;

    protected BondAccount() {}
    public BondAccount(User user, String name, String currency) {
        this.account = new Account(user, name, currency, AccountKind.BOND);
    }
    public Long getId() { return id; }
    public Account getAccount() { return account; }
    public String getName() { return account.getName(); }
    public String getCurrency() { return account.getCurrency(); }
}
