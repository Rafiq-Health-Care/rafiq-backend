CREATE TABLE IF NOT EXISTS consultation_summary (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    summary TEXT,
    recovery_plan TEXT,
    medicine_summary JSONB,
    required_lab_test JSONB,
    doctor_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    consultation_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uq_consultation_summary_consultation UNIQUE (consultation_id),
    CONSTRAINT fk_consultation_summary_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id),
    CONSTRAINT fk_consultation_summary_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_consultation_summary_consultation FOREIGN KEY (consultation_id) REFERENCES consultation (id)
);

CREATE INDEX IF NOT EXISTS idx_consultation_summary_consultation ON consultation_summary (consultation_id);