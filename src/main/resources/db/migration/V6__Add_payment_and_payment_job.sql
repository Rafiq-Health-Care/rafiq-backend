
CREATE TABLE payments (
    id                UUID           PRIMARY KEY DEFAULT gen_random_uuid(),

    consultation_id   UUID           NOT NULL REFERENCES consultation (id)
                                     ON DELETE CASCADE,

    patient_id        UUID           NOT NULL REFERENCES users (id)
                                     ON DELETE CASCADE,

    payment_intent_id VARCHAR(255)   NOT NULL,
    client_secret     VARCHAR(255)   NOT NULL,

    amount            NUMERIC(10, 2) NOT NULL,
    currency          VARCHAR(3)     NOT NULL,

    status            VARCHAR(50)    NOT NULL,

    expires_at        TIMESTAMP      NOT NULL,
    paid_at           TIMESTAMP,

    created_at        TIMESTAMP      NOT NULL,
    updated_at        TIMESTAMP,

    CONSTRAINT uq_payments_consultation   UNIQUE (consultation_id),
    CONSTRAINT uq_payments_payment_intent UNIQUE (payment_intent_id)
);

CREATE INDEX idx_payments_patient ON payments (patient_id);
CREATE INDEX idx_payments_status  ON payments (status);

CREATE TABLE payment_jobs (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),

    payment_id UUID        NOT NULL REFERENCES payments (id)
                           ON DELETE CASCADE,

    status     VARCHAR(50) NOT NULL,

    run_at     TIMESTAMP   NOT NULL,
    created_at TIMESTAMP   NOT NULL,

    CONSTRAINT uq_payment_jobs_payment UNIQUE (payment_id)
);

CREATE INDEX idx_job_status_run_at ON payment_jobs (status, run_at);
