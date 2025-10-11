CREATE TABLE comment_likes (
    like_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    comment_id  BIGINT NOT NULL,
    is_liked    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comments_likes_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_likes_comment FOREIGN KEY (comment_id)
        REFERENCES comments(comment_id) ON DELETE CASCADE,

    CONSTRAINT uq_user_comment UNIQUE (user_id, comment_id)
);
