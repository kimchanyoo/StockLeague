CREATE TABLE stock_yearly_prices (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    year INT NOT NULL,
    open_price BIGINT,
    high_price BIGINT,
    low_price BIGINT,
    close_price BIGINT,
    volume BIGINT,

    CONSTRAINT uq_stock_year UNIQUE (stock_id, year),
    CONSTRAINT fk_yearly_prices_stock
        FOREIGN KEY (stock_id) REFERENCES stocks(stock_id) ON DELETE CASCADE
);

CREATE INDEX idx_stock_year ON stock_yearly_prices (stock_id, year);


