CREATE TABLE custom_days (
    reminder_id UUID NOT NULL,
    day VARCHAR(255),
    CONSTRAINT fk_custom_days_medicine FOREIGN KEY (reminder_id) REFERENCES medicine (id) ON DELETE CASCADE
);
