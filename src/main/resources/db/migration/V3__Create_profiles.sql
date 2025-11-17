-- Create patient_profile table
CREATE TABLE patient_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- Create doctor_profile table
-- Note: social_links_id FK will be added in V4 after social_links table is created
CREATE TABLE doctor_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    description TEXT,
    hospital_name VARCHAR(255),
    personal_photo VARCHAR(255),
    national_id VARCHAR(255),
    hospital_id VARCHAR(255),
    public_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'IN_REVIEW',
    specialization_id UUID NOT NULL,
    social_links_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID,
    CONSTRAINT fk_doctor_profile_specialization FOREIGN KEY (specialization_id) REFERENCES specialization(id)
);

-- Now add foreign keys to users table that reference profiles
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'fk_user_doctor_profile'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT fk_user_doctor_profile 
            FOREIGN KEY (doctor_profile_id) REFERENCES doctor_profile(id) ON DELETE CASCADE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'fk_user_patient_profile'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT fk_user_patient_profile 
            FOREIGN KEY (patient_profile_id) REFERENCES patient_profile(id) ON DELETE CASCADE;
    END IF;
END $$;

