-- =============================================================================
-- V14 · Sync entities: consultation slots + refunds + doctor balance
-- =============================================================================

-- Doctor: add balance
ALTER TABLE doctor
    ADD COLUMN IF NOT EXISTS balance NUMERIC(10, 2) NOT NULL DEFAULT 0;

-- Consultation slots table (new)
CREATE TABLE consultation_slot (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id        UUID        NOT NULL REFERENCES doctor (id),
    start_time       TIMESTAMP   NOT NULL,
    duration_minutes INTEGER     NOT NULL,
    end_time         TIMESTAMP   NOT NULL,
    status           VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    -- BaseEntity --
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ,
    created_by       UUID        NOT NULL,
    updated_by       UUID,
    deleted          BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_by       VARCHAR(255),
    deleted_at       TIMESTAMPTZ
);

-- Consultation: add slot_id and backfill from existing consultation fields
ALTER TABLE consultation
    ADD COLUMN IF NOT EXISTS slot_id UUID;

UPDATE consultation
SET slot_id = gen_random_uuid()
WHERE slot_id IS NULL;

INSERT INTO consultation_slot (
    id,
    doctor_id,
    start_time,
    duration_minutes,
    end_time,
    status,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_by,
    deleted_at
)
SELECT
    c.slot_id,
    c.doctor_id,
    c.start_time,
    c.duration_minutes,
    c.end_time,
    'AVAILABLE',
    c.created_at,
    c.updated_at,
    c.created_by,
    c.updated_by,
    c.deleted,
    c.deleted_by,
    c.deleted_at
FROM consultation c
WHERE NOT EXISTS (
    SELECT 1 FROM consultation_slot cs WHERE cs.id = c.slot_id
);

ALTER TABLE consultation
    ADD CONSTRAINT uq_consultation_slot UNIQUE (slot_id),
    ADD CONSTRAINT fk_consultation_slot FOREIGN KEY (slot_id)
        REFERENCES consultation_slot (id);

ALTER TABLE consultation
    ALTER COLUMN slot_id SET NOT NULL,
    ALTER COLUMN patient_id SET NOT NULL;

-- Drop legacy consultation columns now represented by consultation_slot
ALTER TABLE consultation
    DROP COLUMN doctor_id,
    DROP COLUMN start_time,
    DROP COLUMN duration_minutes,
    DROP COLUMN end_time;

DROP INDEX IF EXISTS idx_consultation_doctor;

-- Remove unused specialization on consultation
DROP INDEX IF EXISTS consultation_specialization_idx;

ALTER TABLE consultation
    DROP COLUMN IF EXISTS specialization;

-- Access token should be unique when present
CREATE UNIQUE INDEX IF NOT EXISTS uq_consultation_access_token
    ON consultation (access_token)
    WHERE access_token IS NOT NULL;

-- Consultation logs table (new)
CREATE TABLE consultation_logs (
    id                UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id   UUID      NOT NULL REFERENCES consultation (id) ON DELETE CASCADE,
    doctor_enter_time TIMESTAMP,
    doctor_leave_time TIMESTAMP,
    patient_enter_time TIMESTAMP,
    patient_leave_time TIMESTAMP,
    CONSTRAINT uq_consultation_logs_consultation UNIQUE (consultation_id)
);

-- Medicine: remove unused doctor_id
ALTER TABLE medicine
    DROP COLUMN IF EXISTS doctor_id;

DROP INDEX IF EXISTS idx_medicine_doctor;

-- Refund request table (new)
CREATE TABLE refund_request (
    id               UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    status           VARCHAR(50)    NOT NULL DEFAULT 'PENDING',
    amount           NUMERIC(10, 2),
    payment_id       UUID           NOT NULL REFERENCES payments (id) ON DELETE CASCADE,
    patient_id       UUID           NOT NULL REFERENCES patient (id) ON DELETE CASCADE,
    consultation_id  UUID           NOT NULL REFERENCES consultation (id) ON DELETE CASCADE,
    stripe_refund_id VARCHAR(255),
    -- BaseEntity --
    created_at       TIMESTAMPTZ    NOT NULL,
    updated_at       TIMESTAMPTZ,
    created_by       UUID           NOT NULL,
    updated_by       UUID,
    deleted          BOOLEAN        NOT NULL DEFAULT FALSE,
    deleted_by       VARCHAR(255),
    deleted_at       TIMESTAMPTZ,
    CONSTRAINT uq_refund_request_payment UNIQUE (payment_id),
    CONSTRAINT uq_refund_request_consultation UNIQUE (consultation_id)
);

-- Payments: add refund_request_id + payment_provider
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS refund_request_id UUID,
    ADD COLUMN IF NOT EXISTS payment_provider  VARCHAR(50);

ALTER TABLE payments
    ADD CONSTRAINT uq_payments_refund_request UNIQUE (refund_request_id),
    ADD CONSTRAINT fk_payments_refund_request FOREIGN KEY (refund_request_id)
        REFERENCES refund_request (id);

-- Update doctor search view to use consultation_slot
CREATE OR REPLACE VIEW doctor_search_view AS
SELECT
        d.id                  AS doctor_id,
        d.personal_photo      AS personal_photo,
        u.first_name          AS first_name,
        u.last_name           AS last_name,
        d.specialization      AS specialization,
        d.price               AS price,
        d.rating              AS rating,
        d.experience_years    AS experience_years,
        u.gender              AS gender,
        MIN(cs.start_time)    AS next_available
FROM doctor d
JOIN users u ON d.id = u.id
JOIN consultation_slot cs ON cs.doctor_id = d.id
WHERE cs.start_time >= NOW()
    AND cs.deleted = FALSE
GROUP BY d.id, d.personal_photo, u.first_name, u.last_name,
                 d.specialization, d.price, d.rating, d.experience_years, u.gender;
