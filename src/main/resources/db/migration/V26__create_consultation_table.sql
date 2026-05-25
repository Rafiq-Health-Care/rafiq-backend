CREATE TABLE IF NOT EXISTS consultation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slot_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    access_token VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT chk_consultation_status CHECK (
        status IN ('PENDING', 'COMPLETED', 'CANCELLED', 'UPCOMING', 'LIVE')
    ),
    CONSTRAINT fk_consultation_slot FOREIGN KEY (slot_id) REFERENCES consultation_slot (id),
    CONSTRAINT fk_consultation_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT uq_consultation_access_token UNIQUE (access_token)
);

CREATE INDEX IF NOT EXISTS idx_consultation_patient ON consultation (patient_id);
CREATE INDEX IF NOT EXISTS idx_consultation_slot ON consultation (slot_id);
CREATE INDEX IF NOT EXISTS idx_consultation_status ON consultation (status);
