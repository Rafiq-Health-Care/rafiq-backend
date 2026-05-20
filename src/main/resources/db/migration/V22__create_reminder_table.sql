CREATE TABLE reminder (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vibrate BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(255) NOT NULL,
    next_reminder TIMESTAMP,
    disable BOOLEAN NOT NULL DEFAULT FALSE,
    medicine_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    group_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uq_reminder_medicine UNIQUE (medicine_id),
    CONSTRAINT fk_reminder_medicine FOREIGN KEY (medicine_id) REFERENCES medicine (id),
    CONSTRAINT fk_reminder_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_reminder_group FOREIGN KEY (group_id) REFERENCES groups (id)
);

CREATE INDEX patient_idx ON reminder (patient_id);
CREATE INDEX medicine_idx ON reminder (medicine_id);
CREATE INDEX status_idx ON reminder (status);
CREATE INDEX reminder_idx ON reminder (id);
