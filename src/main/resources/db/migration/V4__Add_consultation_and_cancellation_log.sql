-- =============================================
-- V4: Consultation & Cancellation Log Tables
-- =============================================

CREATE TABLE consultation (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id      UUID         NOT NULL REFERENCES users(id),
    patient_id     UUID                  REFERENCES users(id),
    date           DATE         NOT NULL,
    start_time     TIME         NOT NULL,
    duration_minutes INTEGER    NOT NULL,
    end_date       DATE,
    status         VARCHAR(50)  NOT NULL DEFAULT 'AVAILABLE',
    notes          TEXT,
    price          NUMERIC(10, 2) NOT NULL,
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_by     VARCHAR(255),
    deleted_at     TIMESTAMP WITH TIME ZONE,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE,
    created_by     UUID         NOT NULL,
    updated_by     UUID,
    CONSTRAINT uk_doctor_date_start UNIQUE (doctor_id, date, start_time)
);

CREATE INDEX doctor_idx  ON consultation(doctor_id);
CREATE INDEX patient_con_idx ON consultation(patient_id);
CREATE INDEX status_idx  ON consultation(status);

CREATE TABLE cancellation_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID         NOT NULL REFERENCES consultation(id),
    reason          TEXT         NOT NULL,
    cancelled_by    UUID         NOT NULL REFERENCES users(id),
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_by      VARCHAR(255),
    deleted_at      TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE,
    created_by      UUID         NOT NULL,
    updated_by      UUID
);
