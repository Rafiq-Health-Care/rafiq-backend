-- Create social_links table
CREATE TABLE social_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    facebook VARCHAR(255),
    twitter VARCHAR(255),
    instagram VARCHAR(255),
    linkedin VARCHAR(255),
    youtube VARCHAR(255),
    whatsapp VARCHAR(255),
    website VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- Add foreign key constraint to doctor_profile for social_links_id
-- This was created in V3 but the FK constraint needs to be added now that social_links exists
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'fk_doctor_profile_social_links'
    ) THEN
        ALTER TABLE doctor_profile
            ADD CONSTRAINT fk_doctor_profile_social_links 
            FOREIGN KEY (social_links_id) REFERENCES social_links(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Create lab table
CREATE TABLE lab (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    logo VARCHAR(255),
    public_id VARCHAR(255),
    social_links_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID,
    CONSTRAINT fk_lab_social_links FOREIGN KEY (social_links_id) REFERENCES social_links(id) ON DELETE CASCADE
);

-- Create address table
CREATE TABLE address (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    street VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    country VARCHAR(255),
    postal_code VARCHAR(255),
    user_id UUID,
    lab_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID,
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_address_lab FOREIGN KEY (lab_id) REFERENCES lab(id) ON DELETE CASCADE
);

-- Create token table
CREATE TABLE token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(255) NOT NULL,
    token_type VARCHAR(50) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID,
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

