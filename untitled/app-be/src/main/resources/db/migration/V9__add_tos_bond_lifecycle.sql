-- Immutable issue catalogue. Existing accounts and transfers are preserved.
CREATE TABLE bond_issues (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(16) NOT NULL UNIQUE,
    type VARCHAR(16) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    nominal DECIMAL(19,2) NOT NULL,
    annual_rate DECIMAL(7,4) NOT NULL,
    tax_rate DECIMAL(7,4) NOT NULL,
    early_redemption_fee DECIMAL(19,2) NOT NULL,
    duration_years INT NOT NULL,
    sale_from DATE NOT NULL,
    sale_until DATE NOT NULL,
    calculation_rule VARCHAR(64) NOT NULL,
    source_url VARCHAR(500) NOT NULL,
    CONSTRAINT ck_bond_issue_terms CHECK (type = 'TOS' AND currency = 'PLN' AND nominal = 100
        AND annual_rate >= 0 AND tax_rate BETWEEN 0 AND 100 AND early_redemption_fee >= 0
        AND duration_years = 3 AND sale_until >= sale_from)
);

INSERT INTO bond_issues (code, type, currency, nominal, annual_rate, tax_rate, early_redemption_fee,
    duration_years, sale_from, sale_until, calculation_rule, source_url)
VALUES ('TOS1029', 'TOS', 'PLN', 100.00, 4.4000, 19.0000, 1.00, 3, '2026-10-01', '2026-10-31',
    'TOS_FIXED_ANNUAL_V1', 'https://www.obligacjeskarbowe.pl/media_files/c15a26ae-de6e-4b89-a137-cb16687a7b0a.pdf');

CREATE TABLE bonds (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    bond_account_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    original_account_id BIGINT NOT NULL,
    original_account_name VARCHAR(255) NOT NULL,
    issue_id BIGINT NOT NULL,
    purchase_date DATE NOT NULL,
    maturity_date DATE NOT NULL,
    original_quantity INT NOT NULL,
    remaining_quantity INT NOT NULL,
    pending_quantity INT NOT NULL DEFAULT 0,
    unit_purchase_price DECIMAL(19,2) NOT NULL,
    capitalized_years INT NOT NULL DEFAULT 0,
    capitalized_unit_value DECIMAL(19,2) NOT NULL,
    KEY idx_bonds_user (user_id),
    CONSTRAINT fk_bonds_account FOREIGN KEY (bond_account_id) REFERENCES bond_accounts(id),
    CONSTRAINT fk_bonds_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bonds_issue FOREIGN KEY (issue_id) REFERENCES bond_issues(id),
    CONSTRAINT ck_bond_quantities CHECK (original_quantity > 0 AND remaining_quantity BETWEEN 0 AND original_quantity
        AND pending_quantity BETWEEN 0 AND remaining_quantity AND unit_purchase_price > 0
        AND capitalized_years BETWEEN 0 AND 3 AND maturity_date > purchase_date),
    CONSTRAINT ck_bond_detached CHECK (bond_account_id IS NOT NULL OR remaining_quantity = 0)
);

CREATE TABLE bond_operations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    bond_id BIGINT NOT NULL,
    event_key VARCHAR(100) NOT NULL UNIQUE,
    request_id VARCHAR(36) NULL UNIQUE,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    instruction_date DATE NOT NULL,
    accrual_until DATE NOT NULL,
    settlement_date DATE NOT NULL,
    quantity INT NOT NULL,
    principal DECIMAL(19,2) NOT NULL,
    gross_interest DECIMAL(19,2) NOT NULL,
    fee DECIMAL(19,2) NOT NULL,
    tax DECIMAL(19,2) NOT NULL,
    net_cash DECIMAL(19,2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    processed_at DATETIME(6) NULL,
    KEY idx_bond_operations_due (status, settlement_date),
    CONSTRAINT fk_bond_operation_bond FOREIGN KEY (bond_id) REFERENCES bonds(id),
    CONSTRAINT ck_bond_operation CHECK (quantity > 0 AND principal >= 0 AND gross_interest >= 0
        AND fee >= 0 AND tax >= 0 AND status IN ('PENDING', 'SETTLED')
        AND type IN ('PURCHASE', 'CAPITALIZATION', 'EARLY_REDEMPTION', 'MATURITY_REDEMPTION')
        AND (status = 'PENDING' OR processed_at IS NOT NULL))
);
