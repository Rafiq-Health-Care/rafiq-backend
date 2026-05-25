CREATE TABLE IF NOT EXISTS patient (
    id UUID PRIMARY KEY,
    description TEXT,
    height INTEGER NOT NULL DEFAULT 0,
    blood_type VARCHAR(255),
    smoke_status VARCHAR(255),
    cigarettes_per_day INTEGER NOT NULL DEFAULT 0,
    last_smoked TIMESTAMP,
    alcoholism BOOLEAN NOT NULL DEFAULT FALSE,
    drinks_per_week INTEGER NOT NULL DEFAULT 0,
    pregnant BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_patient_blood_type CHECK (
        blood_type IN (
            'A_POSITIVE', 'A_NEGATIVE', 'B_POSITIVE', 'B_NEGATIVE',
            'AB_POSITIVE', 'AB_NEGATIVE', 'O_POSITIVE', 'O_NEGATIVE'
        ) OR blood_type IS NULL
    ),
    CONSTRAINT chk_patient_smoke_status CHECK (smoke_status IN ('YES', 'NO', 'FORMER') OR smoke_status IS NULL),
    CONSTRAINT fk_patient_user FOREIGN KEY (id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_patient_id ON patient (id);
