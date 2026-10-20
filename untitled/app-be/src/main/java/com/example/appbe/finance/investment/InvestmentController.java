package com.example.appbe.finance.investment;

import jakarta.validation.Valid;
import com.example.appbe.finance.account.AccountDeletedResponse;
import com.example.appbe.finance.account.AccountDeletionService;
import com.example.appbe.finance.account.AccountKind;
import com.example.appbe.finance.account.DeleteAccountRequest;
import java.time.YearMonth;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/investments")
public class InvestmentController {
    private final DepositService service;
    private final InvestmentQueryService queries;
    private final AccountDeletionService deletionService;
    private final InvestmentCreationService creation;
    public InvestmentController(DepositService service, InvestmentQueryService queries, AccountDeletionService deletionService,
            InvestmentCreationService creation) {
        this.service = service; this.queries = queries;
        this.deletionService = deletionService;
        this.creation = creation;
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public InvestmentMutationResponse createAccount(@Valid @RequestBody InvestmentAccountRequest request,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return creation.create(request, month);
    }
    @PutMapping("/{investmentId}/deposit")
    public InvestmentMutationResponse configureDeposit(@PathVariable Long investmentId, @Valid @RequestBody DepositRequest request,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return service.configureDeposit(investmentId, request, month);
    }
    @PostMapping("/deposits") @ResponseStatus(HttpStatus.CREATED)
    public InvestmentMutationResponse createDeposit(@Valid @RequestBody DepositRequest request,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return service.createDeposit(request, month);
    }
    @GetMapping("/{investmentId}")
    public InvestmentDetailsDto details(@PathVariable Long investmentId) { return queries.details(investmentId); }
    @DeleteMapping("/{investmentId}")
    public AccountDeletedResponse deleteAccount(@PathVariable Long investmentId, @Valid @RequestBody DeleteAccountRequest request,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return deletionService.delete(investmentId, queries.kind(investmentId), request, month);
    }
}
