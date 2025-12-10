-- Specialization table
CREATE TABLE doctor_schema.specialization (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description VARCHAR(255),
    code VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Doctor table (no BaseEntity, has @Embedded SocialLinks)
CREATE TABLE doctor_schema.doctor (
    id UUID PRIMARY KEY,
    description VARCHAR(255),
    hospital_name VARCHAR(255),
    personal_photo VARCHAR(255),
    national_id VARCHAR(255),
    hospital_id VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
    specialization_id UUID NOT NULL REFERENCES doctor_schema.specialization(id),
    facebook VARCHAR(255),
    twitter VARCHAR(255),
    instagram VARCHAR(255),
    linkedin VARCHAR(255),
    youtube VARCHAR(255),
    whatsapp VARCHAR(255),
    website VARCHAR(255),
    public_id VARCHAR(255),
    status VARCHAR(50)
);

CREATE INDEX specialization_idx ON doctor_schema.doctor(specialization_id);

-- Medical Certifications table
CREATE TABLE doctor_schema.medical_certifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description VARCHAR(255),
    code VARCHAR(255),
    photo VARCHAR(255),
    doctor_id UUID NOT NULL REFERENCES doctor_schema.doctor(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);
