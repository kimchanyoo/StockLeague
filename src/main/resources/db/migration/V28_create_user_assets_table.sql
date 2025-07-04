CREATE TABLE user_assets (
    user_id BIGINT PRIMARY KEY,
    cash_balance NUMERIC NOT NULL DEFAULT 10000000,
    total_valuation NUMERIC NOT NULL DEFAULT 10000000,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_assets_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);