ALTER TABLE notifications
    DROP CONSTRAINT fk_notifications_user;

ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE;