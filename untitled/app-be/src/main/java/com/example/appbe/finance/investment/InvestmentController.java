package com.example.appbe.finance.investment;

import jakarta.validation.Valid;
import java.time.YearMonth;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/investments")
public class InvestmentController {
    private final InvestmentService service;
    private final InvestmentQueryService queries;
    public InvestmentController(InvestmentService service, InvestmentQueryService queries) {
        this.service = service; this.queries = queries;
    }
    @PostMapping("/deposits") @ResponseStatus(HttpStatus.CREATED)
    public InvestmentMutationResponse createDeposit(@Valid @RequestBody DepositRequest request,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return service.createDeposit(request, month);
    }
    @GetMapping("/{investmentId}")
    public InvestmentDetailsDto details(@PathVariable Long investmentId) { return queries.details(investmentId); }
}
