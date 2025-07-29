CREATE TABLE reserved_cash (
    reserved_cash_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,

    reserved_amount NUMERIC(20, 4) NOT NULL CHECK (reserved_amount >= 0),
    refunded BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_reserved_cash_order UNIQUE (order_id),

    CONSTRAINT fk_reserved_cash_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,

    CONSTRAINT fk_reserved_cash_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE
);


CREATE INDEX idx_reserved_cash_user ON reserved_cash (user_id);
CREATE INDEX idx_reserved_cash_order ON reserved_cash (order_id);