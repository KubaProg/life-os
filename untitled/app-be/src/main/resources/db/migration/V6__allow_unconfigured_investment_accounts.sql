-- A new account has no capital or product terms until configured in its details view.
ALTER TABLE investment_accounts
    DROP CHECK ck_investment_principal,
    DROP CHECK ck_deposit_terms,
    ADD CONSTRAINT ck_investment_principal CHECK (principal >= 0),
    ADD CONSTRAINT ck_deposit_terms CHECK (type <> 'DEPOSIT' OR (
        (principal = 0 AND funded_on IS NULL AND settled_on IS NULL AND
         annual_rate IS NULL AND tax_rate IS NULL AND start_date IS NULL AND maturity_date IS NULL AND
         capitalization IS NULL AND calculation_rule IS NULL)
        OR
        (principal > 0 AND
         annual_rate IS NOT NULL AND annual_rate >= 0 AND annual_rate <= 100 AND
         tax_rate IS NOT NULL AND tax_rate >= 0 AND tax_rate <= 100 AND
         start_date IS NOT NULL AND maturity_date IS NOT NULL AND maturity_date > start_date AND
         capitalization IS NOT NULL AND capitalization = 'AT_MATURITY' AND
         calculation_rule IS NOT NULL AND calculation_rule = 'FIXED_ACT_ACT_MATURITY_V1')
    ));
