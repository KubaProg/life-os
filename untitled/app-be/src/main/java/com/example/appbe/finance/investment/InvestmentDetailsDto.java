package com.example.appbe.finance.investment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvestmentDetailsDto(InvestmentDto investment, List<ValuationDto> valuations,
                                    List<InvestmentOperationDto> operations) {
    public record ValuationDto(LocalDate date, BigDecimal currentValue, BigDecimal grossProfit,
                               BigDecimal estimatedTax, BigDecimal netProfit, String source) {
        public static ValuationDto from(InvestmentValuation value) {
            return new ValuationDto(value.getValuationDate(), value.getCurrentValue(), value.getGrossProfit(),
                    value.getEstimatedTax(), value.getNetProfit(), value.getSource());
        }
    }
}
