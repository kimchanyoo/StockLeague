CREATE TABLE stock_monthly_prices (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    open_price BIGINT,
    high_price BIGINT,
    low_price BIGINT,
    close_price BIGINT,
    volume BIGINT,

    CONSTRAINT uq_stock_month UNIQUE (stock_id, year, month),
    CONSTRAINT fk_monthly_prices_stock
        FOREIGN KEY (stock_id) REFERENCES stocks(stock_id) ON DELETE CASCADE
);

CREATE INDEX idx_stock_month ON stock_monthly_prices (stock_id, year, month);


