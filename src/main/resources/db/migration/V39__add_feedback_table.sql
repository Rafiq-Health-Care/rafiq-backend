CREATE TABLE IF NOT EXISTS feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feedback TEXT,
    rating NUMERIC(3, 2) NOT NULL DEFAULT 0,
    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    consultation_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uq_feedback_consultation UNIQUE (consultation_id),
    CONSTRAINT fk_feedback_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_feedback_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id),
    CONSTRAINT fk_feedback_consultation FOREIGN KEY (consultation_id) REFERENCES consultation (id)
);

CREATE INDEX IF NOT EXISTS idx_feedback_patient ON feedback (patient_id);
CREATE INDEX IF NOT EXISTS idx_feedback_doctor ON feedback (doctor_id);
CREATE INDEX IF NOT EXISTS idx_feedback_consultation ON feedback (consultation_id);