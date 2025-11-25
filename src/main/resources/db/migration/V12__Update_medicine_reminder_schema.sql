-- Migration to update Medicine and Reminder tables to match new entity structure

-- Add new columns to medicine table
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS reminder_frequency VARCHAR(50);

-- Update custom_days table to reference medicine instead of reminder
ALTER TABLE custom_days DROP CONSTRAINT IF EXISTS fk_custom_days_reminder;
ALTER TABLE custom_days RENAME COLUMN reminder_id TO medicine_id;
ALTER TABLE custom_days ADD CONSTRAINT fk_custom_days_medicine 
    FOREIGN KEY (medicine_id) REFERENCES medicine(id) ON DELETE CASCADE;

-- Drop index and recreate with new column name
DROP INDEX IF EXISTS idx_custom_days_reminder_id;
CREATE INDEX idx_custom_days_medicine_id ON custom_days(medicine_id);

-- Remove old columns from reminder table
ALTER TABLE reminder DROP COLUMN IF EXISTS hour;
ALTER TABLE reminder DROP COLUMN IF EXISTS minute;
ALTER TABLE reminder DROP COLUMN IF EXISTS frequency;

-- Add new column to reminder table
ALTER TABLE reminder ADD COLUMN IF NOT EXISTS next_reminder TIMESTAMP;
