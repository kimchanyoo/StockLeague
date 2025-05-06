CREATE TABLE inquiry_answers (
    answer_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    inquiry_id   BIGINT NOT NULL UNIQUE,
    user_id      BIGINT NOT NULL,
    content      TEXT NOT NULL,
    answered_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_inquiry_answer_inquiry FOREIGN KEY (inquiry_id) REFERENCES inquiries (inquiry_id),
    CONSTRAINT fk_inquiry_answer_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);
