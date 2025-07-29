ALTER TABLE reserved_cash
    ADD COLUMN refunded_amount NUMERIC(20, 4) NOT NULL
        CHECK (refunded_amount IS NULL OR refunded_amount >= 0);