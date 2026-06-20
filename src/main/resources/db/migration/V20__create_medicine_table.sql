CREATE TABLE IF NOT EXISTS medicine (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dosage VARCHAR(255) NOT NULL,
    frequency VARCHAR(255) NOT NULL,
    reminder_frequency VARCHAR(255),
    status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
    type VARCHAR(255),
    start_date DATE,
    end_date DATE,
    notes TEXT,
    photo_url VARCHAR(255),
    name VARCHAR(255),
    search_vector TSVECTOR,
    drug_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    group_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT chk_medicine_frequency CHECK (
        frequency IN (
            'ONCE', 'TWICE', 'THIRD_TIMES', 'FOUR_TIMES', 'FIVE_TIMES', 'SIX_TIMES',
            'SEVEN_TIMES', 'EIGHT_TIMES', 'NINE_TIMES', 'TEN_TIMES', 'ELEVEN_TIMES',
            'TWELVE_TIMES', 'AS_NEEDED', 'CUSTOM'
        )
    ),
    CONSTRAINT chk_medicine_reminder_frequency CHECK (
        reminder_frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY', 'CUSTOM') OR reminder_frequency IS NULL
    ),
    CONSTRAINT chk_medicine_status CHECK (status IN ('ACTIVE', 'DISCONTINUED', 'INACTIVE')),
    CONSTRAINT chk_medicine_type CHECK (
        type IN (
            'TABLET', 'CAPSULE', 'LIQUID', 'INJECTION', 'TOPICAL', 'INHALER', 'SUPPOSITORY',
            'PATCH', 'DROPS', 'POWDER', 'PRESCRIPTION', 'SUPPLEMENT', 'OTHER'
        ) OR type IS NULL
    ),
    CONSTRAINT fk_medicine_drug FOREIGN KEY (drug_id) REFERENCES drug (id),
    CONSTRAINT fk_medicine_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_medicine_group FOREIGN KEY (group_id) REFERENCES groups (id)
);

CREATE INDEX IF NOT EXISTS idx_medicine_search_vector ON medicine USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS idx_medicine_patient ON medicine (patient_id);
CREATE INDEX IF NOT EXISTS idx_medicine_group ON medicine (group_id);
CREATE INDEX IF NOT EXISTS idx_medicine_id ON medicine (id);
