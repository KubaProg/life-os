package com.example.appbe.finance.overview;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/overview")
public class FinanceOverviewController {

    private final FinanceOverviewService overviewService;

    public FinanceOverviewController(FinanceOverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping
    public FinanceOverviewDto getOverview() {
        return overviewService.getOverview();
    }
}
