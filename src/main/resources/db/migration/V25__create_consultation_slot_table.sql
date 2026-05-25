CREATE TABLE IF NOT EXISTS consultation_slot (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL,
    start_time TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT chk_consultation_slot_status CHECK (
        status IN ('AVAILABLE', 'BOOKED', 'EXPIRED', 'CANCELLED', 'PENDING_PAYMENT')
    ),
    CONSTRAINT fk_consultation_slot_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id)
);

CREATE INDEX IF NOT EXISTS idx_consultation_slot_doctor ON consultation_slot (doctor_id);
CREATE INDEX IF NOT EXISTS idx_consultation_slot_status ON consultation_slot (status);
CREATE INDEX IF NOT EXISTS idx_consultation_slot_id ON consultation_slot (id);
CREATE INDEX IF NOT EXISTS idx_consultation_slot_time ON consultation_slot (start_time, end_time);
