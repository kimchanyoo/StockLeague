CREATE TABLE user_stocks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    quantity NUMERIC(20, 2) NOT NULL DEFAULT 0,
    locked_quantity NUMERIC(20, 2) NOT NULL DEFAULT 0,

    CONSTRAINT fk_user_stock_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_stock_stock FOREIGN KEY (stock_id) REFERENCES stocks(stock_id) ON DELETE CASCADE,

    CONSTRAINT uq_user_stock UNIQUE (user_id, stock_id)
);