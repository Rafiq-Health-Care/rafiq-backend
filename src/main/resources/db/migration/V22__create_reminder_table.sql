CREATE TABLE IF NOT EXISTS reminder (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vibrate BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(255) NOT NULL,
    next_reminder TIMESTAMP,
    disable BOOLEAN NOT NULL DEFAULT FALSE,
    medicine_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    group_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uq_reminder_medicine UNIQUE (medicine_id),
    CONSTRAINT chk_reminder_status CHECK (
        status IN ('UPCOMING', 'TAKEN', 'MISSED', 'SNOOZED', 'SKIPPED', 'SERVED')
    ),
    CONSTRAINT fk_reminder_medicine FOREIGN KEY (medicine_id) REFERENCES medicine (id),
    CONSTRAINT fk_reminder_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_reminder_group FOREIGN KEY (group_id) REFERENCES groups (id)
);

CREATE INDEX IF NOT EXISTS idx_reminder_patient ON reminder (patient_id);
CREATE INDEX IF NOT EXISTS idx_reminder_medicine ON reminder (medicine_id);
CREATE INDEX IF NOT EXISTS idx_reminder_status ON reminder (status);
CREATE INDEX IF NOT EXISTS idx_reminder_id ON reminder (id);
