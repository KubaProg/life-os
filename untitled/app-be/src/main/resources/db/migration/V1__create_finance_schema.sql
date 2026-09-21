CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE financial_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(32) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    current_balance DECIMAL(19, 4) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_financial_accounts PRIMARY KEY (id),
    INDEX idx_financial_accounts_user_id (user_id),
    CONSTRAINT fk_financial_accounts_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    financial_account_id BIGINT NULL,
    type VARCHAR(32) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    transaction_date DATE NOT NULL,
    description VARCHAR(500) NULL,
    category VARCHAR(100) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_transactions PRIMARY KEY (id),
    INDEX idx_transactions_user_id (user_id),
    CONSTRAINT fk_transactions_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_transactions_financial_account
        FOREIGN KEY (financial_account_id) REFERENCES financial_accounts (id)
);
