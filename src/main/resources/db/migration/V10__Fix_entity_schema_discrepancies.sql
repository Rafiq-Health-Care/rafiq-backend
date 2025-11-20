-- Fix discrepancies between entities and database schema

-- 1. Fix users table
-- Change active default from false to true (entity has @Builder.Default private boolean active = true)
ALTER TABLE users ALTER COLUMN active SET DEFAULT true;

-- Add NOT NULL constraint to first_name (entity has @NotBlank)
ALTER TABLE users ALTER COLUMN first_name SET NOT NULL;

-- 2. Fix token table
-- Change token column length from VARCHAR(255) to VARCHAR(1000)
ALTER TABLE token ALTER COLUMN token TYPE VARCHAR(1000);

-- Add UNIQUE constraint if not exists (entity has @Column(unique = true))
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'token_token_key'
    ) THEN
        ALTER TABLE token ADD CONSTRAINT token_token_key UNIQUE (token);
    END IF;
END $$;

-- Add index for token column (entity has @Index(name = "token_idx", columnList = "token"))
CREATE INDEX IF NOT EXISTS token_idx ON token(token);

-- Add index for user_id column (entity has @Index(name = "user_idx", columnList = "user_id"))
CREATE INDEX IF NOT EXISTS user_idx ON token(user_id);

-- 3. Fix medicine table
-- Add NOT NULL constraint to dosage (entity has @NotNull @Column(nullable = false))
ALTER TABLE medicine ALTER COLUMN dosage SET NOT NULL;

-- Change type to nullable (entity has no nullable constraint)
ALTER TABLE medicine ALTER COLUMN type DROP NOT NULL;

-- Change doctor_id to nullable (entity has no nullable constraint on doctor)
DO $$ 
BEGIN
    ALTER TABLE medicine DROP CONSTRAINT IF EXISTS fk_medicine_doctor;
    ALTER TABLE medicine ALTER COLUMN doctor_id DROP NOT NULL;
    ALTER TABLE medicine ADD CONSTRAINT fk_medicine_doctor 
        FOREIGN KEY (doctor_id) REFERENCES doctor_profile(id);
END $$;

-- 4. Fix medicine status default to match entity (@Builder.Default private MedicineStatus status = MedicineStatus.ACTIVE)
ALTER TABLE medicine ALTER COLUMN status SET DEFAULT 'ACTIVE';



