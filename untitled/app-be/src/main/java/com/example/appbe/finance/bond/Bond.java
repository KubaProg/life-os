package com.example.appbe.finance.bond;

import com.example.appbe.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** One purchase lot. Original quantity remains historical; principal uses remaining quantity. */
@Entity
@Table(name = "bonds")
public class Bond {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "bond_account_id") private BondAccount account;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(nullable = false) private Long originalAccountId;
    @Column(nullable = false, length = 255) private String originalAccountName;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "issue_id", nullable = false) private BondIssue issue;
    @Column(nullable = false) private LocalDate purchaseDate;
    @Column(nullable = false) private LocalDate maturityDate;
    @Column(nullable = false) private int originalQuantity;
    @Column(nullable = false) private int remainingQuantity;
    @Column(nullable = false) private int pendingQuantity;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal unitPurchasePrice;
    @Column(nullable = false) private int capitalizedYears;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal capitalizedUnitValue;

    protected Bond() {}
    public Bond(BondAccount account, BondIssue issue, int quantity, LocalDate date) {
        this.account = account; this.user = account.getAccount().getUser();
        this.originalAccountId = account.getId(); this.originalAccountName = account.getName();
        this.issue = issue; this.originalQuantity = quantity; this.remainingQuantity = quantity;
        this.purchaseDate = date; this.maturityDate = date.plusYears(issue.getDurationYears());
        this.unitPurchasePrice = issue.getNominal(); this.capitalizedUnitValue = issue.getNominal();
    }
    public Long getId() { return id; }
    public BondAccount getAccount() { return account; }
    public Long getOriginalAccountId() { return originalAccountId; }
    public BondIssue getIssue() { return issue; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public LocalDate getMaturityDate() { return maturityDate; }
    public int getOriginalQuantity() { return originalQuantity; }
    public int getRemainingQuantity() { return remainingQuantity; }
    public int getPendingQuantity() { return pendingQuantity; }
    public int getAvailableQuantity() { return remainingQuantity - pendingQuantity; }
    public BigDecimal getUnitPurchasePrice() { return unitPurchasePrice; }
    public int getCapitalizedYears() { return capitalizedYears; }
    public BigDecimal getCapitalizedUnitValue() { return capitalizedUnitValue; }
    public void capitalize(int years, BigDecimal value) { capitalizedYears = years; capitalizedUnitValue = value; }
    public void reserve(int quantity) { pendingQuantity += quantity; }
    public void settle(int quantity, boolean pending) {
        remainingQuantity -= quantity;
        if (pending) pendingQuantity -= quantity;
    }
    public void detachAccount() { account = null; }
}
