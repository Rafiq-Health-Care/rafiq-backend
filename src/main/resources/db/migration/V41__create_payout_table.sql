CREATE TABLE IF NOT EXISTS payout (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    release_at TIMESTAMPTZ NOT NULL,
    paid_at TIMESTAMPTZ,
    payout_intent_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uq_payout_consultation UNIQUE (consultation_id),
    CONSTRAINT chk_payout_status CHECK (
        status IN ('PENDING', 'PROCESSING', 'PAID', 'FAILED')
    ),
    CONSTRAINT fk_payout_consultation FOREIGN KEY (consultation_id) REFERENCES consultation (id),
    CONSTRAINT fk_payout_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id)
);

CREATE INDEX IF NOT EXISTS idx_payout_id ON payout (id);
CREATE INDEX IF NOT EXISTS idx_payout_consultation ON payout (consultation_id);
CREATE INDEX IF NOT EXISTS idx_payout_doctor ON payout (doctor_id);
CREATE INDEX IF NOT EXISTS idx_payout_status ON payout (status);
CREATE INDEX IF NOT EXISTS idx_payout_release_at ON payout (release_at);
