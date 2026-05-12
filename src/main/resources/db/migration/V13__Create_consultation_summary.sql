CREATE TABLE consultation_summary (
    id                 UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    summary            TEXT,
    recovery_plan      TEXT,
    medicine_summary   JSONB,
    required_lab_test  JSONB,
    doctor_id          UUID        NOT NULL     REFERENCES doctor (id),
    patient_id         UUID        NOT NULL     REFERENCES patient (id),
    consultation_id    UUID        NOT NULL     REFERENCES consultation (id),
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ,
    created_by         UUID        NOT NULL,
    updated_by         UUID,
    deleted            BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_by         VARCHAR(255),
    deleted_at         TIMESTAMPTZ,
    CONSTRAINT uq_consultation_summary_consultation UNIQUE (consultation_id)
);

CREATE INDEX idx_consultation_summary_patient ON consultation_summary (patient_id);
CREATE INDEX idx_consultation_summary_doctor ON consultation_summary (doctor_id);
