package com.example.appbe.finance.investment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Embedded in investment_accounts; not a separate table or entity. */
@Embeddable
public class DepositTerms {
    @Column(name = "annual_rate", precision = 9, scale = 4)
    private BigDecimal annualRate;
    @Column(name = "tax_rate", precision = 9, scale = 4)
    private BigDecimal taxRate;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "maturity_date")
    private LocalDate maturityDate;
    @Column(length = 32)
    private String capitalization;
    @Column(name = "calculation_rule", length = 64)
    private String calculationRule;
    protected DepositTerms() {}
    public DepositTerms(BigDecimal annualRate, BigDecimal taxRate, LocalDate start, LocalDate maturity) {
        this.annualRate = annualRate; this.taxRate = taxRate; this.startDate = start; this.maturityDate = maturity;
        this.capitalization = "AT_MATURITY"; this.calculationRule = "FIXED_ACT_ACT_MATURITY_V1";
    }
    public BigDecimal getAnnualRate() { return annualRate; }
    public BigDecimal getTaxRate() { return taxRate; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getMaturityDate() { return maturityDate; }
    public String getCapitalization() { return capitalization; }
    public String getCalculationRule() { return calculationRule; }
}
