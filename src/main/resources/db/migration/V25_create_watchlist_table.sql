CREATE TABLE watchlists (
    watchlist_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ,

    CONSTRAINT fk_watchlist_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,

    CONSTRAINT fk_watchlist_stock
        FOREIGN KEY (stock_id) REFERENCES stocks (id) ON DELETE CASCADE,

    CONSTRAINT uq_watchlist_user_stock UNIQUE (user_id, stock_id)
);
