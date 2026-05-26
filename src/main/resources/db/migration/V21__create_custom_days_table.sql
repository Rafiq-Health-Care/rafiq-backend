CREATE TABLE IF NOT EXISTS custom_days (
    reminder_id UUID NOT NULL,
    day VARCHAR(255) NOT NULL,
    CONSTRAINT pk_custom_days PRIMARY KEY (reminder_id, day),
    CONSTRAINT fk_custom_days_medicine FOREIGN KEY (reminder_id) REFERENCES medicine (id) ON DELETE CASCADE
);
