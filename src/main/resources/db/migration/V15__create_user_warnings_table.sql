CREATE TABLE user_warnings (
    warning_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    comment_id BIGINT NOT NULL,
    admin_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_warnings_user
        FOREIGN KEY (user_id) REFERENCES users(user_id),

    CONSTRAINT fk_user_warnings_comment
        FOREIGN KEY (comment_id) REFERENCES comments(comment_id),

    CONSTRAINT fk_user_warnings_admin
        FOREIGN KEY (admin_id) REFERENCES users(user_id)
);
