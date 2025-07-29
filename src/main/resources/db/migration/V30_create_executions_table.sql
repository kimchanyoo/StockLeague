CREATE TABLE order_executions (
    execution_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id BIGINT NOT NULL,
    executed_price NUMERIC(20, 2) NOT NULL,
    executed_amount NUMERIC(20, 2) NOT NULL,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_execution_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE
);

CREATE INDEX idx_order_executions_order
    ON order_executions (order_id);