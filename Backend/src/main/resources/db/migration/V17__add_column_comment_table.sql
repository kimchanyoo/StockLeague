ALTER TABLE comments
    ADD COLUMN processed_by_id BIGINT;

ALTER TABLE comments
    ADD CONSTRAINT fk_comment_processed_by
        FOREIGN KEY (processed_by_id) REFERENCES users(user_id);

ALTER TABLE comments
    ADD COLUMN action_taken VARCHAR(20);