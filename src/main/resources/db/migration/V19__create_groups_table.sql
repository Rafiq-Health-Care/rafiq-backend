CREATE TABLE IF NOT EXISTS groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description VARCHAR(255),
    icon_id VARCHAR(255),
    color VARCHAR(255),
    patient_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_groups_patient FOREIGN KEY (patient_id) REFERENCES patient (id)
);

CREATE INDEX IF NOT EXISTS idx_groups_patient ON groups (patient_id);
CREATE INDEX IF NOT EXISTS idx_groups_id ON groups (id);
