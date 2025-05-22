ALTER TABLE notices DROP CONSTRAINT fk_notice_user;
ALTER TABLE inquiries DROP CONSTRAINT fk_inquiries_user;
ALTER TABLE inquiry_answers DROP CONSTRAINT fk_inquiry_answer_user;
ALTER TABLE inquiry_answers DROP CONSTRAINT fk_inquiry_answer_inquiry;

ALTER TABLE notices
    ADD CONSTRAINT fk_notice_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

ALTER TABLE inquiries
    ADD CONSTRAINT fk_inquiries_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

ALTER TABLE inquiry_answers
    ADD CONSTRAINT fk_inquiry_answer_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

ALTER TABLE inquiry_answers
    ADD CONSTRAINT fk_inquiry_answer_inquiry
        FOREIGN KEY (inquiry_id) REFERENCES inquiries(inquiry_id) ON DELETE CASCADE;