ALTER TABLE financial_accounts
    CHANGE COLUMN current_balance opening_balance DECIMAL(19, 4) NOT NULL,
    ADD COLUMN balance_start_date DATE NULL AFTER opening_balance;

UPDATE financial_accounts
SET balance_start_date = CURRENT_DATE
WHERE balance_start_date IS NULL;

ALTER TABLE financial_accounts
    MODIFY COLUMN balance_start_date DATE NOT NULL;
