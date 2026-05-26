CREATE TABLE IF NOT EXISTS refund_request (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    amount NUMERIC(10, 2),
    payment_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    consultation_id UUID NOT NULL,
    stripe_refund_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uq_refund_request_payment UNIQUE (payment_id),
    CONSTRAINT uq_refund_request_patient UNIQUE (patient_id),
    CONSTRAINT uq_refund_request_consultation UNIQUE (consultation_id),
    CONSTRAINT chk_refund_request_status CHECK (
        status IN ('PENDING', 'COMPLETED', 'FAILED', 'PROCESSING')
    ),
    CONSTRAINT fk_refund_request_payment FOREIGN KEY (payment_id) REFERENCES payments (id),
    CONSTRAINT fk_refund_request_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_refund_request_consultation FOREIGN KEY (consultation_id) REFERENCES consultation (id)
);

CREATE INDEX IF NOT EXISTS idx_refund_request_payment ON refund_request (payment_id);
CREATE INDEX IF NOT EXISTS idx_refund_request_patient ON refund_request (patient_id);
CREATE INDEX IF NOT EXISTS idx_refund_request_status ON refund_request (status);
CREATE INDEX IF NOT EXISTS idx_refund_request_stripe_refund_id ON refund_request (stripe_refund_id);
