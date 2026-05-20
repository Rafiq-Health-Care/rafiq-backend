CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    refund_request_id UUID,
    payment_intent_id VARCHAR(255) NOT NULL,
    client_secret VARCHAR(255) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP NOT NULL,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    payment_provider VARCHAR(255),
    CONSTRAINT uq_payments_consultation UNIQUE (consultation_id),
    CONSTRAINT uq_payments_payment_intent UNIQUE (payment_intent_id),
    CONSTRAINT uq_payments_refund_request UNIQUE (refund_request_id),
    CONSTRAINT fk_payments_consultation FOREIGN KEY (consultation_id) REFERENCES consultation (id),
    CONSTRAINT fk_payments_patient FOREIGN KEY (patient_id) REFERENCES users (id)
);

CREATE INDEX idx_payments_patient ON payments (patient_id);
CREATE INDEX idx_payments_status ON payments (status);
