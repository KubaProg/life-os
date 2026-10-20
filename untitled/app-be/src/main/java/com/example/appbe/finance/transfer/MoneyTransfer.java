package com.example.appbe.finance.transfer;

import com.example.appbe.user.User;
import com.example.appbe.finance.account.Account;
import com.example.appbe.finance.account.AccountKind;
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;
    @Column(name = "deleted_source_id") private Long deletedSourceId;
    @Enumerated(EnumType.STRING) @Column(name = "deleted_source_kind", length = 32) private AccountKind deletedSourceKind;
    @Column(name = "deleted_source_name", length = 255) private String deletedSourceName;
    @Column(name = "deleted_destination_id") private Long deletedDestinationId;
    @Enumerated(EnumType.STRING) @Column(name = "deleted_destination_kind", length = 32) private AccountKind deletedDestinationKind;
    @Column(name = "deleted_destination_name", length = 255) private String deletedDestinationName;
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
    public AccountReference sourceReference() {
        return sourceAccount == null ? new AccountReference(deletedSourceKind, deletedSourceId)
                : new AccountReference(sourceAccount.getKind(), sourceAccount.getId());
    }
    public AccountReference destinationReference() {
        return destinationAccount == null ? new AccountReference(deletedDestinationKind, deletedDestinationId)
                : new AccountReference(destinationAccount.getKind(), destinationAccount.getId());
    }
    public String sourceName() { return sourceAccount == null ? deletedSourceName + " (usunięte konto)" : sourceAccount.getName(); }
    public String destinationName() { return destinationAccount == null ? deletedDestinationName + " (usunięte konto)" : destinationAccount.getName(); }
    public void detachAccount(Long accountId) {
        if (sourceAccount != null && sourceAccount.getId().equals(accountId)) {
            deletedSourceId = sourceAccount.getId(); deletedSourceKind = sourceAccount.getKind(); deletedSourceName = sourceAccount.getName();
            sourceAccount = null;
        }
        if (destinationAccount != null && destinationAccount.getId().equals(accountId)) {
            deletedDestinationId = destinationAccount.getId(); deletedDestinationKind = destinationAccount.getKind(); deletedDestinationName = destinationAccount.getName();
            destinationAccount = null;
        }
    }
}
