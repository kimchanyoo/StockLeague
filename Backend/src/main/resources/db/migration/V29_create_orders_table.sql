CREATE TABLE orders (
    order_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,

    order_type VARCHAR(10) NOT NULL CHECK (order_type IN ('BUY', 'SELL')),
    status VARCHAR(20) NOT NULL CHECK (
        status IN (
                   'WAITING',
                   'PARTIALLY_EXECUTED',
                   'EXECUTED',
                   'CANCELED',
                   'EXPIRED',
                   'FAILED'
                  )
        ),

    order_price NUMERIC(20, 2) NOT NULL,
    order_amount NUMERIC(20, 2) NOT NULL,
    executed_amount NUMERIC(20, 2) NOT NULL DEFAULT 0,
    remaining_amount NUMERIC(20, 2) NOT NULL,
    average_executed_price NUMERIC(20, 2),

    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    executed_at TIMESTAMP,

    CONSTRAINT fk_order_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,

    CONSTRAINT fk_order_stock
        FOREIGN KEY (stock_id) REFERENCES stocks (stock_id) ON DELETE CASCADE
);

CREATE INDEX idx_orders_stock_status_type
    ON orders (stock_id, status, order_type);

