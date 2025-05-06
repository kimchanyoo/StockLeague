CREATE TABLE users (
    user_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    role            VARCHAR(10) NOT NULL,
    nickname        VARCHAR(10) NOT NULL,
    agreed_to_terms TINYINT(1) NOT NULL,
    is_over_fifteen TINYINT(1) NOT NULL,
    provider        VARCHAR(10) NOT NULL,
    oauth_id        VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_users_oauth_provider UNIQUE (oauth_id, provider)
);
