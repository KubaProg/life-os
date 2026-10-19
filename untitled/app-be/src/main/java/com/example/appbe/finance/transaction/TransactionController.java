package com.example.appbe.finance.transaction;

import jakarta.validation.Valid;
import java.time.YearMonth;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionCreatedResponse createTransaction(
            @Valid @RequestBody TransactionRequest request,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return transactionService.createTransaction(request, month);
    }

    @PutMapping("/{transactionId}")
    public TransactionUpdatedResponse updateTransaction(
            @PathVariable Long transactionId,
            @Valid @RequestBody TransactionRequest request,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return transactionService.updateTransaction(transactionId, request, month);
    }

    @DeleteMapping("/{transactionId}")
    public TransactionDeletedResponse deleteTransaction(
            @PathVariable Long transactionId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return transactionService.deleteTransaction(transactionId, month);
    }
}
