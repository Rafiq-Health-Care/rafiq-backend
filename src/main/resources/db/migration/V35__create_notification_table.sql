CREATE TABLE IF NOT EXISTS notification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    user_id UUID NOT NULL,
    data JSONB,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_notification_user ON notification (user_id);
CREATE INDEX IF NOT EXISTS idx_notification_id ON notification (id);
