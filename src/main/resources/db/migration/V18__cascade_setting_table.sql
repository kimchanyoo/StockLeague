ALTER TABLE comment_reports
    DROP CONSTRAINT fk_comment_reports_reporter;

ALTER TABLE comment_reports
    ADD CONSTRAINT fk_comment_reports_reporter
        FOREIGN KEY (reporter_id) REFERENCES users(user_id) ON DELETE CASCADE;