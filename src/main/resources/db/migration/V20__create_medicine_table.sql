CREATE TABLE medicine (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dosage VARCHAR(255) NOT NULL,
    frequency VARCHAR(255) NOT NULL,
    reminder_frequency VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    type VARCHAR(255),
    start_date TIMESTAMPTZ,
    end_date TIMESTAMPTZ,
    notes VARCHAR(255),
    photo_url VARCHAR(255),
    photo_public_id VARCHAR(255),
    name VARCHAR(255),
    search_vector TSVECTOR,
    drug_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    group_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_medicine_drug FOREIGN KEY (drug_id) REFERENCES drug (id),
    CONSTRAINT fk_medicine_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_medicine_group FOREIGN KEY (group_id) REFERENCES groups (id)
);

CREATE INDEX medicine_search_vector_idx ON medicine USING GIN (search_vector);
CREATE INDEX patient_medicine_idx ON medicine (patient_id);
