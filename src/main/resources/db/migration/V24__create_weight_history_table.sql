CREATE TABLE weight_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    weight DOUBLE PRECISION NOT NULL DEFAULT 0,
    date DATE,
    patient_id UUID NOT NULL,
    CONSTRAINT fk_weight_history_patient FOREIGN KEY (patient_id) REFERENCES patient (id)
);
