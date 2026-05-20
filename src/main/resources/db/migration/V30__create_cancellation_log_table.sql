CREATE TABLE cancellation_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID NOT NULL,
    reason TEXT NOT NULL,
    cancelled_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uq_cancellation_log_consultation UNIQUE (consultation_id),
    CONSTRAINT fk_cancellation_log_consultation FOREIGN KEY (consultation_id) REFERENCES consultation (id),
    CONSTRAINT fk_cancellation_log_cancelled_by FOREIGN KEY (cancelled_by) REFERENCES users (id)
);
