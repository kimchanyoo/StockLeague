CREATE TABLE stocks (
    stock_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    stock_ticker  VARCHAR(15) UNIQUE NOT NULL,
    stock_name    VARCHAR(100) NOT NULL,
    list_date     TIMESTAMP,
    market_type   VARCHAR(10) NOT NULL
);

CREATE INDEX idx_fulltext_ticker_name ON stocks
    USING GIN (to_tsvector('simple', stock_ticker || ' ' || stock_name));