CREATE TABLE patient (
    id UUID PRIMARY KEY,
    description VARCHAR(255),
    height INTEGER NOT NULL DEFAULT 0,
    weight DOUBLE PRECISION NOT NULL DEFAULT 0,
    blood_type VARCHAR(255),
    smoke_status VARCHAR(255),
    cigarettes_per_day INTEGER NOT NULL DEFAULT 0,
    last_smoked TIMESTAMPTZ,
    alcoholism BOOLEAN NOT NULL DEFAULT FALSE,
    drinks_per_week INTEGER NOT NULL DEFAULT 0,
    pregnant BOOLEAN NOT NULL DEFAULT FALSE,
    occupation VARCHAR(255),
    emergency_contact_name VARCHAR(255),
    emergency_contact_phone VARCHAR(255),
    CONSTRAINT fk_patient_user FOREIGN KEY (id) REFERENCES users (id) ON DELETE CASCADE
);
