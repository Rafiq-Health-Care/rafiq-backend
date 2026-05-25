CREATE TABLE IF NOT EXISTS reminder_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status VARCHAR(255),
    "timestamp" TIMESTAMP,
    reminder_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT chk_reminder_log_status CHECK (
        status IN ('UPCOMING', 'TAKEN', 'MISSED', 'SNOOZED', 'SKIPPED', 'SERVED') OR status IS NULL
    ),
    CONSTRAINT fk_reminder_log_reminder FOREIGN KEY (reminder_id) REFERENCES reminder (id),
    CONSTRAINT fk_reminder_log_patient FOREIGN KEY (patient_id) REFERENCES patient (id)
);

CREATE INDEX IF NOT EXISTS idx_reminder_log_reminder ON reminder_log (reminder_id);
