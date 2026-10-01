INSERT INTO users (id, name, email)
VALUES (1, 'Local User', 'local@life-os.app');

INSERT INTO financial_accounts (
    user_id,
    name,
    type,
    currency,
    opening_balance,
    balance_start_date,
    active,
    created_at,
    updated_at
)
VALUES
    (1, 'Konto osobiste', 'BANK_ACCOUNT', 'PLN', 8500.0000, '2026-10-01', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (1, 'Konto oszczędnościowe', 'SAVINGS', 'PLN', 25000.0000, '2026-10-01', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (1, 'Gotówka', 'CASH', 'PLN', 600.0000, '2026-10-01', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));
