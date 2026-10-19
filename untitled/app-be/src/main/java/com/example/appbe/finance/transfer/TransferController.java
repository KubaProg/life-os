package com.example.appbe.finance.transfer;

import jakarta.validation.Valid;
import java.time.YearMonth;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/transfers")
public class TransferController {
    private final TransferService service;
    public TransferController(TransferService service) { this.service = service; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse create(@Valid @RequestBody TransferRequest request,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return service.transfer(request, month);
    }
}
