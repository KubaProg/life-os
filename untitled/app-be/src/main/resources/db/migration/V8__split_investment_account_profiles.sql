-- Keep shared accounts.id and every historical transfer reference unchanged.
CREATE TABLE bond_accounts (
    id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_bond_account_identity FOREIGN KEY (id) REFERENCES accounts(id)
);

-- Preserve previously creatable ETF/OTHER accounts as cash-only brokerage profiles.
CREATE TABLE brokerage_accounts (
    id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_brokerage_account_identity FOREIGN KEY (id) REFERENCES accounts(id),
    CONSTRAINT ck_brokerage_type CHECK (type IN ('ETF', 'OTHER'))
);

INSERT INTO bond_accounts (id)
SELECT id FROM investment_accounts WHERE type = 'BOND';
INSERT INTO brokerage_accounts (id, type)
SELECT id, type FROM investment_accounts WHERE type IN ('ETF', 'OTHER');

ALTER TABLE accounts DROP CHECK ck_accounts_kind;
UPDATE accounts a JOIN investment_accounts i ON i.id = a.id
SET a.kind = CASE i.type
    WHEN 'DEPOSIT' THEN 'DEPOSIT'
    WHEN 'BOND' THEN 'BOND'
    ELSE 'BROKERAGE'
END;
ALTER TABLE accounts ADD CONSTRAINT ck_accounts_kind
    CHECK (kind IN ('ORDINARY', 'DEPOSIT', 'BOND', 'BROKERAGE'));

-- Only deposits supported money movements before V8, so deleted investment
-- endpoints in the transfer history refer to deposits.
UPDATE money_transfers SET deleted_source_kind = 'DEPOSIT' WHERE deleted_source_kind = 'INVESTMENT';
UPDATE money_transfers SET deleted_destination_kind = 'DEPOSIT' WHERE deleted_destination_kind = 'INVESTMENT';

-- Copy profiles first, then remove only those copied to their dedicated tables.
-- Existing deposit operations/valuations keep their FK links through the rename.
DELETE FROM investment_accounts WHERE type IN ('BOND', 'ETF', 'OTHER');
RENAME TABLE investment_accounts TO deposit_accounts;
ALTER TABLE deposit_accounts
    DROP CHECK ck_deposit_terms,
    DROP COLUMN type;
ALTER TABLE deposit_accounts ADD CONSTRAINT ck_deposit_terms CHECK (
    (principal = 0 AND funded_on IS NULL AND settled_on IS NULL AND
     annual_rate IS NULL AND tax_rate IS NULL AND start_date IS NULL AND maturity_date IS NULL AND
     capitalization IS NULL AND calculation_rule IS NULL)
    OR
    (principal > 0 AND
     annual_rate IS NOT NULL AND annual_rate BETWEEN 0 AND 100 AND
     tax_rate IS NOT NULL AND tax_rate BETWEEN 0 AND 100 AND
     start_date IS NOT NULL AND maturity_date IS NOT NULL AND maturity_date > start_date AND
     capitalization IS NOT NULL AND capitalization = 'AT_MATURITY' AND
     calculation_rule IS NOT NULL AND calculation_rule = 'FIXED_ACT_ACT_MATURITY_V1')
);
