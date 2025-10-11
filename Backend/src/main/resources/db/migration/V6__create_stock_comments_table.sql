CREATE TABLE comments
(
    comment_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    stock_id    BIGINT    NOT NULL,
    user_id     BIGINT    NOT NULL,
    parent_id   BIGINT REFERENCES comments (comment_id) ON DELETE CASCADE,
    content     TEXT      NOT NULL,
    like_count  INTEGER   NOT NULL DEFAULT 0,
    reply_count INTEGER   NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_stock FOREIGN KEY (stock_id)
        REFERENCES stocks (stock_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_stock_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE INDEX idx_comments_stock_id ON comments (stock_id);

CREATE INDEX idx_comments_parent_id ON comments (parent_id);

CREATE INDEX idx_comments_user_id ON comments (user_id);