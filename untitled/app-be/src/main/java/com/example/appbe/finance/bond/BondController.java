package com.example.appbe.finance.bond;

import com.example.appbe.finance.investment.InvestmentMutationResponse;
import com.example.appbe.finance.overview.FinanceOverviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/finance/bonds")
public class BondController {
    private final BondService bonds;
    private final FinanceOverviewService overview;
    public BondController(BondService bonds, FinanceOverviewService overview) { this.bonds = bonds; this.overview = overview; }

    @GetMapping("/issues")
    public List<BondDtos.Issue> issues() { return bonds.issues(); }
    @GetMapping("/accounts/{accountId}")
    public BondDtos.Details details(@PathVariable Long accountId) { return bonds.details(accountId); }
    @PostMapping("/accounts/{accountId}/purchases")
    public InvestmentMutationResponse purchase(@PathVariable Long accountId, @Valid @RequestBody BondDtos.PurchaseRequest request,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        bonds.purchase(accountId, request);
        return new InvestmentMutationResponse(accountId, overview.getOverview(month));
    }
    @GetMapping("/accounts/{accountId}/purchase-quote")
    public BondDtos.PurchaseQuote purchaseQuote(@PathVariable Long accountId, @RequestParam Long issueId,
            @RequestParam @Min(1) int quantity, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return bonds.purchaseQuote(accountId, issueId, quantity, date);
    }
    @GetMapping("/accounts/{accountId}/positions/{bondId}/redemption-quote")
    public BondDtos.RedemptionQuote quote(@PathVariable Long accountId, @PathVariable Long bondId,
            @RequestParam @Min(1) int quantity, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return bonds.quote(accountId, bondId, quantity, date);
    }
    @PostMapping("/accounts/{accountId}/positions/{bondId}/redemptions")
    public InvestmentMutationResponse redeem(@PathVariable Long accountId, @PathVariable Long bondId,
            @Valid @RequestBody BondDtos.RedemptionRequest request,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        bonds.redeem(accountId, bondId, request);
        return new InvestmentMutationResponse(accountId, overview.getOverview(month));
    }
}
