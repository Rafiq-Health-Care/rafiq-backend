-- Company table
CREATE TABLE medication_schema.company (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    country VARCHAR(255),
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Active Ingredient table
CREATE TABLE medication_schema.active_ingredient (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Drug table
CREATE TABLE medication_schema.drug (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trade_name VARCHAR(255),
    drug_group VARCHAR(255),
    dosage_form VARCHAR(255),
    route VARCHAR(255),
    pharmacology TEXT,
    price DOUBLE PRECISION,
    search_vector tsvector,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX drug_search_vector_idx ON medication_schema.drug USING GIN(search_vector);

CREATE OR REPLACE FUNCTION medication_schema.drug_search_vector_update() RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', COALESCE(NEW.trade_name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.drug_group, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.pharmacology, '')), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER drug_search_vector_trigger
    BEFORE INSERT OR UPDATE OF trade_name, drug_group, pharmacology
    ON medication_schema.drug
    FOR EACH ROW
    EXECUTE FUNCTION medication_schema.drug_search_vector_update();

-- Drug-Company join table
CREATE TABLE medication_schema.drug_company (
    drug_id UUID NOT NULL REFERENCES medication_schema.drug(id) ON DELETE CASCADE,
    company_id UUID NOT NULL REFERENCES medication_schema.company(id) ON DELETE CASCADE,
    PRIMARY KEY (drug_id, company_id)
);

-- Drug-Active Ingredient join table
CREATE TABLE medication_schema.drug_active_ingredient (
    drug_id UUID NOT NULL REFERENCES medication_schema.drug(id) ON DELETE CASCADE,
    active_ingredient_id UUID NOT NULL REFERENCES medication_schema.active_ingredient(id) ON DELETE CASCADE,
    PRIMARY KEY (drug_id, active_ingredient_id)
);

-- Groups table
CREATE TABLE medication_schema.groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description VARCHAR(255),
    icon_public_id VARCHAR(255),
    icon_url VARCHAR(255),
    color VARCHAR(50),
    patient_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Medicine table
CREATE TABLE medication_schema.medicine (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dosage VARCHAR(255) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    reminder_frequency VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    type VARCHAR(50),
    start_date TIMESTAMP WITH TIME ZONE,
    end_date TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    photo_url VARCHAR(255),
    photo_public_id VARCHAR(255),
    name VARCHAR(255),
    search_vector tsvector,
    drug_id UUID NOT NULL REFERENCES medication_schema.drug(id),
    doctor_id UUID,
    patient_id UUID,
    group_id UUID REFERENCES medication_schema.groups(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX medicine_search_vector_idx ON medication_schema.medicine USING GIN(search_vector);
CREATE INDEX patient_medicine_idx ON medication_schema.medicine(patient_id);
CREATE INDEX doctor_medicine_idx ON medication_schema.medicine(doctor_id);

CREATE OR REPLACE FUNCTION medication_schema.medicine_search_vector_update() RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', COALESCE(NEW.name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.notes, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.dosage, '')), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER medicine_search_vector_trigger
    BEFORE INSERT OR UPDATE OF name, notes, dosage
    ON medication_schema.medicine
    FOR EACH ROW
    EXECUTE FUNCTION medication_schema.medicine_search_vector_update();

-- Custom Days collection table
CREATE TABLE medication_schema.custom_days (
    reminder_id UUID NOT NULL REFERENCES medication_schema.medicine(id) ON DELETE CASCADE,
    day VARCHAR(50)
);

-- Reminder table
CREATE TABLE medication_schema.reminder (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vibrate BOOLEAN,
    status VARCHAR(50),
    next_reminder TIMESTAMP,
    disable BOOLEAN DEFAULT FALSE,
    medicine_id UUID NOT NULL UNIQUE REFERENCES medication_schema.medicine(id) ON DELETE CASCADE,
    patient_id UUID,
    group_id UUID REFERENCES medication_schema.groups(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX patient_idx ON medication_schema.reminder(patient_id);
CREATE INDEX medicine_idx ON medication_schema.reminder(medicine_id);

-- Reminder Log table
CREATE TABLE medication_schema.reminder_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status VARCHAR(50),
    timestamp TIMESTAMP,
    reminder_id UUID NOT NULL REFERENCES medication_schema.reminder(id) ON DELETE CASCADE,
    patient_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);
