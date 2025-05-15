CREATE TABLE users (
    ser_id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role            VARCHAR(10) NOT NULL,
    nickname        VARCHAR(10) NOT NULL,
    agreed_to_terms BOOLEAN NOT NULL,
    is_over_fifteen BOOLEAN NOT NULL,
    provider        VARCHAR(10) NOT NULL,
    oauth_id        VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_users_oauth_provider UNIQUE (oauth_id, provider)
);