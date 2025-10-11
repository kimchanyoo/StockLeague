ALTER TABLE users
    ADD COLUMN ban_reason VARCHAR(255),
    ADD COLUMN banned_at TIMESTAMP;

