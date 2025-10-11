CREATE TABLE notices (
    notice_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    title       VARCHAR(200) NOT NULL,
    category    VARCHAR(50) NOT NULL,
    content     TEXT NOT NULL,
    is_pinned   BOOLEAN DEFAULT FALSE NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at  TIMESTAMP NULL,
    CONSTRAINT fk_notice_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE INDEX idx_fulltext_title_content ON notices
    USING GIN (to_tsvector('english', title || ' ' || content));