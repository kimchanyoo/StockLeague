CREATE TABLE inquiries (
    inquiry_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    category     VARCHAR(255) NOT NULL,
    title        VARCHAR(255) NOT NULL,
    content      TEXT NOT NULL,
    status       VARCHAR(10) NOT NULL CHECK (status IN ('WAITING', 'ANSWERED', 'REOPENED', 'CLOSED')),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at   TIMESTAMP NULL,
    answered_at  TIMESTAMP NULL,
    nickname     VARCHAR(255) NOT NULL,
    CONSTRAINT fk_inquiries_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE INDEX idx_inquiries_user_id ON inquiries (user_id);