CREATE TABLE stock_weekly_prices (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    year INT NOT NULL,
    week INT NOT NULL,
    open_price BIGINT,
    high_price BIGINT,
    low_price BIGINT,
    close_price BIGINT,
    volume BIGINT,

    CONSTRAINT uq_stock_week UNIQUE (stock_id, year, week),
    CONSTRAINT fk_weekly_prices_stock
        FOREIGN KEY (stock_id) REFERENCES stocks(stock_id) ON DELETE CASCADE
);

CREATE INDEX idx_stock_week ON stock_weekly_prices (stock_id, year, week);
