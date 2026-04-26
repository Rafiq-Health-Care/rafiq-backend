-- =============================================================================
-- V4 · TimeSlot: replace date+TIME columns with full TIMESTAMP columns
-- =============================================================================

ALTER TABLE consultation
    DROP CONSTRAINT uk_doctor_date_start,
    DROP COLUMN date,
    DROP COLUMN start_time,
    DROP COLUMN end_time;

ALTER TABLE consultation
    ADD COLUMN start_time TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN end_time   TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE consultation
    ALTER COLUMN start_time DROP DEFAULT,
    ALTER COLUMN end_time   DROP DEFAULT;

ALTER TABLE consultation
    ADD CONSTRAINT uk_doctor_date_start UNIQUE (doctor_id, start_time);
