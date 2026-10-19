CREATE TABLE investment_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(160) NOT NULL,
    type VARCHAR(32) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    principal DECIMAL(19,4) NOT NULL,
    annual_rate DECIMAL(9,4) NULL,
    tax_rate DECIMAL(9,4) NULL,
    start_date DATE NULL,
    maturity_date DATE NULL,
    capitalization VARCHAR(32) NULL,
    calculation_rule VARCHAR(64) NULL,
    funded_on DATE NULL,
    settled_on DATE NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_investment_account_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT ck_investment_principal CHECK (principal > 0),
    CONSTRAINT ck_deposit_terms CHECK (type <> 'DEPOSIT' OR (
        annual_rate IS NOT NULL AND annual_rate >= 0 AND annual_rate <= 100 AND
        tax_rate IS NOT NULL AND tax_rate >= 0 AND tax_rate <= 100 AND
        start_date IS NOT NULL AND maturity_date IS NOT NULL AND maturity_date > start_date AND
        capitalization IS NOT NULL AND capitalization = 'AT_MATURITY' AND
        calculation_rule IS NOT NULL AND calculation_rule = 'FIXED_ACT_ACT_MATURITY_V1'
    ))
);
CREATE TABLE money_transfers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    request_id VARCHAR(36) NOT NULL,
    source_account_id BIGINT NULL,
    destination_account_id BIGINT NULL,
    source_investment_id BIGINT NULL,
    destination_investment_id BIGINT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    transfer_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_transfer_request UNIQUE (request_id),
    INDEX idx_transfer_user_date (user_id, transfer_date),
    CONSTRAINT fk_transfer_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_transfer_source_cash FOREIGN KEY (source_account_id) REFERENCES financial_accounts(id),
    CONSTRAINT fk_transfer_destination_cash FOREIGN KEY (destination_account_id) REFERENCES financial_accounts(id),
    CONSTRAINT fk_transfer_source_investment FOREIGN KEY (source_investment_id) REFERENCES investment_accounts(id),
    CONSTRAINT fk_transfer_destination_investment FOREIGN KEY (destination_investment_id) REFERENCES investment_accounts(id),
    CONSTRAINT ck_transfer_amount CHECK (amount > 0),
    CONSTRAINT ck_transfer_source CHECK ((source_account_id IS NOT NULL AND source_investment_id IS NULL) OR
                                       (source_account_id IS NULL AND source_investment_id IS NOT NULL)),
    CONSTRAINT ck_transfer_destination CHECK ((destination_account_id IS NOT NULL AND destination_investment_id IS NULL) OR
                                            (destination_account_id IS NULL AND destination_investment_id IS NOT NULL)),
    CONSTRAINT ck_transfer_different_cash CHECK (source_account_id IS NULL OR destination_account_id IS NULL OR source_account_id <> destination_account_id),
    CONSTRAINT ck_transfer_different_investment CHECK (source_investment_id IS NULL OR destination_investment_id IS NULL OR source_investment_id <> destination_investment_id)
);

CREATE TABLE investment_operations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    investment_account_id BIGINT NOT NULL,
    transfer_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    operation_date DATE NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_investment_operation_kind UNIQUE (investment_account_id, transfer_id, type),
    CONSTRAINT fk_investment_operation_investment FOREIGN KEY (investment_account_id) REFERENCES investment_accounts(id),
    CONSTRAINT fk_investment_operation_transfer FOREIGN KEY (transfer_id) REFERENCES money_transfers(id),
    CONSTRAINT ck_investment_operation_amount CHECK (amount >= 0)
);
CREATE TABLE investment_valuations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    investment_account_id BIGINT NOT NULL,
    valuation_date DATE NOT NULL,
    gross_profit DECIMAL(19,4) NOT NULL,
    estimated_tax DECIMAL(19,4) NOT NULL,
    net_profit DECIMAL(19,4) NOT NULL,
    current_value DECIMAL(19,4) NOT NULL,
    source VARCHAR(64) NOT NULL,
    calculated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_investment_valuation_date UNIQUE (investment_account_id, valuation_date),
    CONSTRAINT fk_investment_valuation_account FOREIGN KEY (investment_account_id) REFERENCES investment_accounts(id)
);
