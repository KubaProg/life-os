package com.example.appbe.finance.investment;

import com.example.appbe.finance.transfer.MoneyTransfer;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "investment_operations", uniqueConstraints = @UniqueConstraint(
        name = "uk_investment_operation_kind", columnNames = {"investment_account_id", "transfer_id", "type"}))
public class InvestmentOperation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_account_id", nullable = false)
    private InvestmentAccount investment;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transfer_id", nullable = false)
    private MoneyTransfer transfer;
    @Column(nullable = false, length = 32)
    private String type;
    @Column(name = "operation_date", nullable = false)
    private LocalDate operationDate;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    protected InvestmentOperation() {}
    public InvestmentOperation(InvestmentAccount investment, MoneyTransfer transfer, String type, LocalDate date,
                               BigDecimal amount) {
        this.investment = investment; this.transfer = transfer; this.type = type;
        this.operationDate = date; this.amount = amount;
    }
    @PrePersist void prePersist() { createdAt = Instant.now(); }
    public Long getId() { return id; }
    public InvestmentAccount getInvestment() { return investment; }
    public MoneyTransfer getTransfer() { return transfer; }
    public String getType() { return type; }
    public LocalDate getOperationDate() { return operationDate; }
    public BigDecimal getAmount() { return amount; }
}
