package com.example.appbe.finance.bond;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Immutable terms of a specific issue, shared by purchased lots. */
@Entity
@Table(name = "bond_issues")
public class BondIssue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 16) private String code;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private BondType type;
    @Column(nullable = false, length = 3) private String currency;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal nominal;
    @Column(nullable = false, precision = 7, scale = 4) private BigDecimal annualRate;
    @Column(nullable = false, precision = 7, scale = 4) private BigDecimal taxRate;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal earlyRedemptionFee;
    @Column(nullable = false) private int durationYears;
    @Column(nullable = false) private LocalDate saleFrom;
    @Column(nullable = false) private LocalDate saleUntil;
    @Column(nullable = false, length = 64) private String calculationRule;
    @Column(nullable = false, length = 500) private String sourceUrl;

    protected BondIssue() {}
    public Long getId() { return id; }
    public String getCode() { return code; }
    public BondType getType() { return type; }
    public String getCurrency() { return currency; }
    public BigDecimal getNominal() { return nominal; }
    public BigDecimal getAnnualRate() { return annualRate; }
    public BigDecimal getTaxRate() { return taxRate; }
    public BigDecimal getEarlyRedemptionFee() { return earlyRedemptionFee; }
    public int getDurationYears() { return durationYears; }
    public LocalDate getSaleFrom() { return saleFrom; }
    public LocalDate getSaleUntil() { return saleUntil; }
    public String getCalculationRule() { return calculationRule; }
    public String getSourceUrl() { return sourceUrl; }
}
