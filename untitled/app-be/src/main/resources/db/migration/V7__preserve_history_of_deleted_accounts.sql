-- Keep historical operations after physically removing an account and its profile.
-- Drop and recreate foreign keys in separate statements. Reusing a foreign-key
-- name within one ALTER TABLE is not supported by MySQL's COPY algorithm.
ALTER TABLE money_transfers
    -- MySQL requires dropping checks that reference columns being modified.
    DROP CHECK ck_transfer_different_accounts,
    DROP FOREIGN KEY fk_transfer_source_account,
    DROP FOREIGN KEY fk_transfer_destination_account;

ALTER TABLE money_transfers
    MODIFY source_account_id BIGINT NULL,
    MODIFY destination_account_id BIGINT NULL,
    ADD deleted_source_id BIGINT NULL,
    ADD deleted_source_kind VARCHAR(32) NULL,
    ADD deleted_source_name VARCHAR(255) NULL,
    ADD deleted_destination_id BIGINT NULL,
    ADD deleted_destination_kind VARCHAR(32) NULL,
    ADD deleted_destination_name VARCHAR(255) NULL,
    ADD CONSTRAINT fk_transfer_source_account FOREIGN KEY (source_account_id) REFERENCES accounts(id),
    ADD CONSTRAINT fk_transfer_destination_account FOREIGN KEY (destination_account_id) REFERENCES accounts(id),
    ADD CONSTRAINT ck_transfer_different_accounts CHECK (source_account_id <> destination_account_id),
    ADD CONSTRAINT ck_transfer_source_history CHECK (source_account_id IS NOT NULL OR
        (deleted_source_id IS NOT NULL AND deleted_source_kind IS NOT NULL AND deleted_source_name IS NOT NULL)),
    ADD CONSTRAINT ck_transfer_destination_history CHECK (destination_account_id IS NOT NULL OR
        (deleted_destination_id IS NOT NULL AND deleted_destination_kind IS NOT NULL AND deleted_destination_name IS NOT NULL));

ALTER TABLE transactions
    ADD deleted_account_id BIGINT NULL,
    ADD deleted_account_name VARCHAR(255) NULL;

ALTER TABLE investment_operations
    DROP FOREIGN KEY fk_investment_operation_investment;

ALTER TABLE investment_operations
    MODIFY investment_account_id BIGINT NULL,
    ADD deleted_investment_currency VARCHAR(3) NULL,
    ADD CONSTRAINT fk_investment_operation_investment FOREIGN KEY (investment_account_id) REFERENCES investment_accounts(id);
