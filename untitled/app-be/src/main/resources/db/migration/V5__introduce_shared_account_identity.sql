-- Forward-only migration. V1-V4 and their Flyway checksums stay unchanged.
-- Ordinary IDs are preserved. Investment IDs are moved above the ordinary ID range,
-- including every existing transfer, operation and valuation reference.
CREATE TABLE accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    kind VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_accounts_user_kind (user_id, kind),
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT ck_accounts_kind CHECK (kind IN ('ORDINARY', 'INVESTMENT'))
);

SET @lifeos_investment_id_offset = (SELECT COALESCE(MAX(id), 0) FROM financial_accounts);

INSERT INTO accounts (id, user_id, name, currency, kind, active, created_at, updated_at)
SELECT id, user_id, name, currency, 'ORDINARY', active, created_at, updated_at
FROM financial_accounts;

INSERT INTO accounts (id, user_id, name, currency, kind, active, created_at, updated_at)
SELECT id + @lifeos_investment_id_offset, user_id, name, currency, 'INVESTMENT', TRUE, created_at, created_at
FROM investment_accounts;

-- Drop only the constraints that must be rebuilt during the ID/reference migration.
ALTER TABLE money_transfers
    DROP FOREIGN KEY fk_transfer_source_cash,
    DROP FOREIGN KEY fk_transfer_destination_cash,
    DROP FOREIGN KEY fk_transfer_source_investment,
    DROP FOREIGN KEY fk_transfer_destination_investment,
    DROP CHECK ck_transfer_source,
    DROP CHECK ck_transfer_destination,
    DROP CHECK ck_transfer_different_cash,
    DROP CHECK ck_transfer_different_investment;

ALTER TABLE investment_operations DROP FOREIGN KEY fk_investment_operation_investment;
ALTER TABLE investment_valuations DROP FOREIGN KEY fk_investment_valuation_account;
ALTER TABLE transactions DROP FOREIGN KEY fk_transactions_financial_account;

UPDATE money_transfers
SET source_account_id = COALESCE(source_account_id, source_investment_id + @lifeos_investment_id_offset),
    destination_account_id = COALESCE(destination_account_id, destination_investment_id + @lifeos_investment_id_offset);

-- Descending updates avoid intermediate primary/unique-key collisions when ID ranges overlap.
UPDATE investment_valuations
SET investment_account_id = investment_account_id + @lifeos_investment_id_offset
ORDER BY investment_account_id DESC;

UPDATE investment_operations
SET investment_account_id = investment_account_id + @lifeos_investment_id_offset
ORDER BY investment_account_id DESC;

UPDATE investment_accounts
SET id = id + @lifeos_investment_id_offset
ORDER BY id DESC;

ALTER TABLE financial_accounts DROP FOREIGN KEY fk_financial_accounts_user;
ALTER TABLE financial_accounts DROP INDEX idx_financial_accounts_user_id;
ALTER TABLE financial_accounts
    MODIFY COLUMN id BIGINT NOT NULL,
    DROP COLUMN user_id,
    DROP COLUMN name,
    DROP COLUMN currency,
    DROP COLUMN active,
    DROP COLUMN created_at,
    DROP COLUMN updated_at,
    ADD CONSTRAINT fk_financial_account_identity FOREIGN KEY (id) REFERENCES accounts(id);

ALTER TABLE investment_accounts DROP FOREIGN KEY fk_investment_account_user;
ALTER TABLE investment_accounts
    MODIFY COLUMN id BIGINT NOT NULL,
    DROP COLUMN user_id,
    DROP COLUMN name,
    DROP COLUMN currency,
    DROP COLUMN created_at,
    ADD CONSTRAINT fk_investment_account_identity FOREIGN KEY (id) REFERENCES accounts(id);

ALTER TABLE money_transfers
    MODIFY COLUMN source_account_id BIGINT NOT NULL,
    MODIFY COLUMN destination_account_id BIGINT NOT NULL,
    DROP COLUMN source_investment_id,
    DROP COLUMN destination_investment_id,
    ADD CONSTRAINT fk_transfer_source_account FOREIGN KEY (source_account_id) REFERENCES accounts(id),
    ADD CONSTRAINT fk_transfer_destination_account FOREIGN KEY (destination_account_id) REFERENCES accounts(id),
    ADD CONSTRAINT ck_transfer_different_accounts CHECK (source_account_id <> destination_account_id);

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_financial_account FOREIGN KEY (financial_account_id) REFERENCES financial_accounts(id);
ALTER TABLE investment_operations
    ADD CONSTRAINT fk_investment_operation_investment FOREIGN KEY (investment_account_id) REFERENCES investment_accounts(id);
ALTER TABLE investment_valuations
    ADD CONSTRAINT fk_investment_valuation_account FOREIGN KEY (investment_account_id) REFERENCES investment_accounts(id);

-- Explicit IDs inserted above advance accounts.AUTO_INCREMENT to the new global range.
-- The ordinary seed from V3 is migrated too; it must not be rewritten or inserted again.
