ALTER TABLE watchlists
    DROP CONSTRAINT fk_watchlist_user;

ALTER TABLE watchlists
    ADD CONSTRAINT fk_watchlist_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE;

ALTER TABLE watchlists
    DROP CONSTRAINT fk_watchlist_stock;

ALTER TABLE watchlists
    ADD CONSTRAINT fk_watchlist_stock FOREIGN KEY (stock_id)
        REFERENCES stocks(stock_id) ON DELETE CASCADE;