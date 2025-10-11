CREATE TABLE stock_daily_prices (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    date DATE NOT NULL,
    open_price BIGINT,
    high_price BIGINT,
    low_price BIGINT,
    close_price BIGINT,
    volume BIGINT,

    CONSTRAINT uq_stock_date UNIQUE (stock_id, date),
    CONSTRAINT fk_daily_prices_stock
        FOREIGN KEY (stock_id) REFERENCES stocks(stock_id) ON DELETE CASCADE
);

CREATE INDEX idx_stock_date ON stock_daily_prices (stock_id, date);


