CREATE TABLE IF NOT EXISTS medical_certifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description TEXT,
    code VARCHAR(255),
    photo VARCHAR(255),
    doctor_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_medical_certifications_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id)
);

CREATE INDEX IF NOT EXISTS idx_medical_certifications_doctor ON medical_certifications (doctor_id);
