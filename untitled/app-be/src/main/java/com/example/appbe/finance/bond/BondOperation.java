package com.example.appbe.finance.bond;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Cash changes only on settlement; dates retain the financial date even after catch-up. */
@Entity
@Table(name = "bond_operations")
public class BondOperation {
    public enum Type { PURCHASE, CAPITALIZATION, EARLY_REDEMPTION, MATURITY_REDEMPTION }
    public enum Status { PENDING, SETTLED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "bond_id", nullable = false) private Bond bond;
    @Column(nullable = false, unique = true, length = 100) private String eventKey;
    @Column(unique = true, length = 36) private String requestId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private Type type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private Status status;
    @Column(nullable = false) private LocalDate instructionDate;
    @Column(nullable = false) private LocalDate accrualUntil;
    @Column(nullable = false) private LocalDate settlementDate;
    @Column(nullable = false) private int quantity;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal principal;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal grossInterest;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal fee;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal tax;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal netCash;
    @Column(nullable = false) private Instant createdAt;
    private Instant processedAt;

    protected BondOperation() {}
    public BondOperation(Bond bond, String eventKey, String requestId, Type type, Status status,
            LocalDate instructionDate, LocalDate accrualUntil, LocalDate settlementDate, int quantity,
            BigDecimal principal, BigDecimal grossInterest, BigDecimal fee, BigDecimal tax, BigDecimal netCash) {
        this.bond = bond; this.eventKey = eventKey; this.requestId = requestId; this.type = type; this.status = status;
        this.instructionDate = instructionDate; this.accrualUntil = accrualUntil; this.settlementDate = settlementDate;
        this.quantity = quantity; this.principal = principal; this.grossInterest = grossInterest;
        this.fee = fee; this.tax = tax; this.netCash = netCash; this.createdAt = Instant.now();
        if (status == Status.SETTLED) processedAt = createdAt;
    }
    public Long getId() { return id; }
    public Bond getBond() { return bond; }
    public String getRequestId() { return requestId; }
    public Type getType() { return type; }
    public Status getStatus() { return status; }
    public LocalDate getInstructionDate() { return instructionDate; }
    public LocalDate getAccrualUntil() { return accrualUntil; }
    public LocalDate getSettlementDate() { return settlementDate; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrincipal() { return principal; }
    public BigDecimal getGrossInterest() { return grossInterest; }
    public BigDecimal getFee() { return fee; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal getNetCash() { return netCash; }
    public BigDecimal netProfit() { return grossInterest.subtract(fee).subtract(tax); }
    public void settle() { status = Status.SETTLED; processedAt = Instant.now(); }
}
