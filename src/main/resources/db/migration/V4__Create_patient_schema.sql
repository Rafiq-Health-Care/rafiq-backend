-- Patient table (no BaseEntity)
CREATE TABLE patient_schema.patient (
    id UUID PRIMARY KEY,
    description VARCHAR(255),
    height INTEGER,
    weight DOUBLE PRECISION,
    blood_type VARCHAR(50),
    smoke_status VARCHAR(50),
    cigarettes_per_day INTEGER,
    last_smoked TIMESTAMP WITH TIME ZONE,
    alcoholism BOOLEAN,
    drinks_per_week INTEGER,
    pregnant BOOLEAN,
    occupation VARCHAR(255),
    emergency_contact_name VARCHAR(255),
    emergency_contact_phone VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255)
);

-- Weight History table (no BaseEntity)
CREATE TABLE patient_schema.weight_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    weight DOUBLE PRECISION,
    date DATE,
    patient_id UUID NOT NULL REFERENCES patient_schema.patient(id) ON DELETE CASCADE
);
