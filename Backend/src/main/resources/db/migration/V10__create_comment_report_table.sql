CREATE TABLE comment_reports (
    report_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    processed_by_id BIGINT,
    reason VARCHAR(255) NOT NULL,
    additional_info TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    action_taken VARCHAR(20),

    CONSTRAINT fk_comment_reports_reporter
        FOREIGN KEY (reporter_id) REFERENCES users(user_id),

    CONSTRAINT fk_comment_reports_processor
        FOREIGN KEY (processed_by_id) REFERENCES users(user_id),

    CONSTRAINT fk_comment_reports_target
        FOREIGN KEY (target_id) REFERENCES comments(comment_id)
);
