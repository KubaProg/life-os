package com.example.appbe.finance.account;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateAccountRequest(
        @Size(max = 255)
        @Pattern(regexp = ".*\\S.*", message = "must not be blank")
        String name,

        FinancialAccountType type,

        @Pattern(regexp = "[A-Z]{3}")
        String currency
) {
}
