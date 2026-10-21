package com.example.appbe.finance.bond;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class BondDtos {
    private BondDtos() {}
    public record PurchaseRequest(@NotNull UUID requestId, @NotNull Long issueId,
            @Min(1) @Max(1000000) int quantity, @NotNull @PastOrPresent LocalDate date) {}
    public record RedemptionRequest(@NotNull UUID requestId, @Min(1) @Max(1000000) int quantity,
            @NotNull @PastOrPresent LocalDate date) {}
    public record Issue(Long id, String code, BondType type, String currency, BigDecimal unitPrice,
            BigDecimal annualRate, BigDecimal taxRate, int durationYears, BigDecimal earlyRedemptionFee,
            LocalDate saleFrom, LocalDate saleUntil, String sourceUrl) {
        public static Issue from(BondIssue issue) {
            return new Issue(issue.getId(), issue.getCode(), issue.getType(), issue.getCurrency(), issue.getNominal(),
                    issue.getAnnualRate(), issue.getTaxRate(), issue.getDurationYears(), issue.getEarlyRedemptionFee(),
                    issue.getSaleFrom(), issue.getSaleUntil(), issue.getSourceUrl());
        }
    }
    public record Position(Long id, String issueCode, LocalDate purchaseDate, LocalDate maturityDate,
            int originalQuantity, int remainingQuantity, int pendingQuantity, int availableQuantity,
            BigDecimal principal, BigDecimal grossValue, BigDecimal currentValue, BigDecimal grossProfit,
            BigDecimal estimatedTax, BigDecimal netProfit, BigDecimal annualRate, BigDecimal realizedProfit,
            LocalDate earliestRedemptionDate, LocalDate latestRedemptionDate, boolean canRedeem, String status) {}
    public record Operation(Long id, Long bondId, String issueCode, BondOperation.Type type, BondOperation.Status status,
            LocalDate instructionDate, LocalDate settlementDate, int quantity, BigDecimal principal,
            BigDecimal grossInterest, BigDecimal fee, BigDecimal tax, BigDecimal netCash) {
        public static Operation from(BondOperation op) {
            return new Operation(op.getId(), op.getBond().getId(), op.getBond().getIssue().getCode(), op.getType(), op.getStatus(),
                    op.getInstructionDate(), op.getSettlementDate(), op.getQuantity(), op.getPrincipal(),
                    op.getGrossInterest(), op.getFee(), op.getTax(), op.getNetCash());
        }
    }
    public record Details(Long accountId, BigDecimal availableCash, BigDecimal principal,
            BigDecimal grossValue, BigDecimal currentValue, BigDecimal grossProfit, BigDecimal estimatedTax,
            BigDecimal netProfit, BigDecimal realizedProfit, List<Position> positions, List<Operation> operations) {}
    public record RedemptionQuote(Long bondId, int quantity, LocalDate instructionDate, LocalDate settlementDate,
            BigDecimal principal, BigDecimal grossInterest, BigDecimal fee, BigDecimal tax, BigDecimal netCash) {}
    public record PurchaseQuote(Long issueId, int quantity, LocalDate date, LocalDate maturityDate,
            BigDecimal cost, BigDecimal availableCash, boolean sufficientFunds) {}
}
