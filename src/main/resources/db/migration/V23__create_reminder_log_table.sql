CREATE TABLE reminder_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status VARCHAR(255),
    "timestamp" TIMESTAMP,
    reminder_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_reminder_log_reminder FOREIGN KEY (reminder_id) REFERENCES reminder (id),
    CONSTRAINT fk_reminder_log_patient FOREIGN KEY (patient_id) REFERENCES patient (id)
);

CREATE INDEX reminder_idx_reminder_log ON reminder_log (reminder_id);
