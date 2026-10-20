package com.example.appbe.finance.investment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Cumulative values at a day boundary, not daily profits to be added together. */
@Entity
@Table(name = "investment_valuations", uniqueConstraints = @UniqueConstraint(
        name = "uk_investment_valuation_date", columnNames = {"investment_account_id", "valuation_date"}))
public class InvestmentValuation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_account_id", nullable = false)
    private DepositAccount investment;
    @Column(name = "valuation_date", nullable = false)
    private LocalDate valuationDate;
    @Column(name = "gross_profit", nullable = false, precision = 19, scale = 4)
    private BigDecimal grossProfit;
    @Column(name = "estimated_tax", nullable = false, precision = 19, scale = 4)
    private BigDecimal estimatedTax;
    @Column(name = "net_profit", nullable = false, precision = 19, scale = 4)
    private BigDecimal netProfit;
    @Column(name = "current_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentValue;
    @Column(nullable = false, length = 64)
    private String source;
    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    protected InvestmentValuation() {}
    public InvestmentValuation(DepositAccount investment, LocalDate date, DepositCalculator.Value value) {
        this.investment = investment; this.valuationDate = date; this.grossProfit = value.grossProfit();
        this.estimatedTax = value.estimatedTax(); this.netProfit = value.netProfit();
        this.currentValue = value.currentValue(); this.source = investment.getDepositTerms().getCalculationRule();
        this.calculatedAt = Instant.now();
    }
    public LocalDate getValuationDate() { return valuationDate; }
    public BigDecimal getGrossProfit() { return grossProfit; }
    public BigDecimal getEstimatedTax() { return estimatedTax; }
    public BigDecimal getNetProfit() { return netProfit; }
    public BigDecimal getCurrentValue() { return currentValue; }
    public String getSource() { return source; }
    public Instant getCalculatedAt() { return calculatedAt; }
}
