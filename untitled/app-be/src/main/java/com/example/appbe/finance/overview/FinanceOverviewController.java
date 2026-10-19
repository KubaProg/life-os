package com.example.appbe.finance.overview;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/finance/overview")
public class FinanceOverviewController {

    private final FinanceOverviewService overviewService;

    public FinanceOverviewController(FinanceOverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping
    public FinanceOverviewDto getOverview(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return overviewService.getOverview(month);
    }
}
