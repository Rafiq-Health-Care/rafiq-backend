CREATE TABLE IF NOT EXISTS lab_test (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description VARCHAR(255),
    code VARCHAR(255),
    pdf VARCHAR(255),
    public_id VARCHAR(255),
    file_type VARCHAR(255),
    doctor_id UUID,
    patient_id UUID,
    date TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_lab_test_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id),
    CONSTRAINT fk_lab_test_patient FOREIGN KEY (patient_id) REFERENCES patient (id)
);

CREATE INDEX IF NOT EXISTS idx_lab_test_patient ON lab_test (patient_id);
