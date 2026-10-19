package com.example.appbe.finance.transfer;

import com.example.appbe.user.User;
import com.example.appbe.finance.account.Account;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "money_transfers")
public class MoneyTransfer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "request_id", nullable = false, unique = true, length = 36)
    private String requestId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_account_id", nullable = false)
    private Account destinationAccount;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "transfer_date", nullable = false)
    private LocalDate date;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    protected MoneyTransfer() {}
    public MoneyTransfer(User user, TransferRequest request, Account sourceAccount, Account destinationAccount) {
        this.user = user;
        this.requestId = request.requestId().toString();
        this.amount = request.amount();
        this.date = request.date();
        this.currency = sourceAccount.getCurrency();
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
    }
    @PrePersist void prePersist() { createdAt = Instant.now(); }
    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public LocalDate getDate() { return date; }
    public Account getSourceAccount() { return sourceAccount; }
    public Account getDestinationAccount() { return destinationAccount; }
}
