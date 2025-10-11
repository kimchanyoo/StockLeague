CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_stocks_name_asc
    ON stocks (stock_name ASC, stock_id ASC);