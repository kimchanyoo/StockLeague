CREATE TABLE notices (
    notice_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    title       VARCHAR(200) NOT NULL,
    category    VARCHAR(50) NOT NULL,
    content     TEXT NOT NULL,
    is_pinned   TINYINT(1) DEFAULT 0 NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP NULL,
    CONSTRAINT fk_notice_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE FULLTEXT INDEX idx_fulltext_title_content ON notices (title, content);
