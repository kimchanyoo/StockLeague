CREATE TABLE stock_minute_prices (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    interval INT NOT NULL,
    candle_time TIMESTAMP NOT NULL,
    open_price BIGINT,
    high_price BIGINT,
    low_price BIGINT,
    close_price BIGINT,
    volume BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_ticker_interval_time UNIQUE (stock_id, interval, candle_time),

    CONSTRAINT fk_minute_prices_stock
        FOREIGN KEY (stock_id) REFERENCES stocks(stock_id) ON DELETE CASCADE
);

CREATE INDEX idx_minute_price_ticker_time ON stock_minute_prices(stock_id, interval, candle_time DESC);
