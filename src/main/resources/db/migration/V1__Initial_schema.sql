-- =============================================
-- V1: Initial Schema - All Tables
-- =============================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable pg_trgm extension for fuzzy/partial text search (similarity function)
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- =============================================
-- BASE TABLES (no foreign key dependencies)
-- =============================================

-- Role table
CREATE TABLE role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- Specialization table
CREATE TABLE specialization (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description VARCHAR(255),
    code VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- Social Links table
CREATE TABLE social_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    facebook VARCHAR(255),
    twitter VARCHAR(255),
    instagram VARCHAR(255),
    linkedin VARCHAR(255),
    youtube VARCHAR(255),
    whatsapp VARCHAR(255),
    website VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- Company table
CREATE TABLE company (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    country VARCHAR(255),
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- Active Ingredient table
CREATE TABLE active_ingredient (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- Drug table
CREATE TABLE drug (
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
    updated_by UUID
);

-- Full-text search index for Drug
CREATE INDEX drug_search_vector_idx ON drug USING GIN(search_vector);

-- Trigram index for partial/fuzzy search on trade_name
CREATE INDEX drug_trade_name_trgm_idx ON drug USING GIN(trade_name gin_trgm_ops);

-- Function to update drug search vector
CREATE OR REPLACE FUNCTION drug_search_vector_update() RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', COALESCE(NEW.trade_name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.drug_group, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.pharmacology, '')), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to automatically update drug search vector
CREATE TRIGGER drug_search_vector_trigger
    BEFORE INSERT OR UPDATE OF trade_name, drug_group, pharmacology
    ON drug
    FOR EACH ROW
    EXECUTE FUNCTION drug_search_vector_update();

-- Drug-Company join the table
CREATE TABLE drug_company (
    drug_id UUID NOT NULL REFERENCES drug(id) ON DELETE CASCADE,
    company_id UUID NOT NULL REFERENCES company(id) ON DELETE CASCADE,
    PRIMARY KEY (drug_id, company_id)
);

-- Drug-Active Ingredient joins the table
CREATE TABLE drug_active_ingredient (
    drug_id UUID NOT NULL REFERENCES drug(id) ON DELETE CASCADE,
    active_ingredient_id UUID NOT NULL REFERENCES active_ingredient(id) ON DELETE CASCADE,
    PRIMARY KEY (drug_id, active_ingredient_id)
);

-- =============================================
-- USERS TABLE
-- =============================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    phone VARCHAR(255),
    age INTEGER,
    active BOOLEAN DEFAULT TRUE,
    locked BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT FALSE,
    notification_token VARCHAR(255),
    gender VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

CREATE INDEX idx_users_email ON users(email);

-- User-Roles join the table
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- =============================================
-- LAB TABLE
-- =============================================

CREATE TABLE lab (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    logo VARCHAR(255),
    public_id VARCHAR(255),
    social_links_id UUID REFERENCES social_links(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- =============================================
-- PROFILE TABLES
-- =============================================

-- Patient Profile table
CREATE TABLE patient_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    description VARCHAR(255),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

CREATE INDEX user_patient_idx ON patient_profile(user_id);

-- Doctor Profile table
CREATE TABLE doctor_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    description VARCHAR(255),
    hospital_name VARCHAR(255),
    personal_photo VARCHAR(255),
    national_id VARCHAR(255),
    hospital_id VARCHAR(255),
    public_id VARCHAR(255),
    status VARCHAR(50),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    specialization_id UUID NOT NULL REFERENCES specialization(id),
    social_links_id UUID REFERENCES social_links(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

CREATE INDEX doctor_profile_idx ON doctor_profile(user_id);
CREATE INDEX specialization_idx ON doctor_profile(specialization_id);

-- Medical Certifications table
CREATE TABLE medical_certifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description VARCHAR(255),
    code VARCHAR(255),
    photo VARCHAR(255),
    doctor_id UUID NOT NULL REFERENCES doctor_profile(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- =============================================
-- ADDRESS TABLE
-- =============================================

CREATE TABLE address (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    street VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    country VARCHAR(255),
    postal_code VARCHAR(255),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    lab_id UUID REFERENCES lab(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- =============================================
-- TOKEN TABLE
-- =============================================

CREATE TABLE token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(1000) NOT NULL UNIQUE,
    token_type VARCHAR(50),
    expiry_date TIMESTAMP WITH TIME ZONE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

CREATE INDEX token_idx ON token(token);
CREATE INDEX user_idx ON token(user_id);

-- =============================================
-- LAB TEST AND RESULTS TABLES
-- =============================================

CREATE TABLE lab_test (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description VARCHAR(255),
    code VARCHAR(255),
    pdf VARCHAR(255),
    public_id VARCHAR(255),
    file_type VARCHAR(255),
    date TIMESTAMP WITH TIME ZONE,
    lab_id UUID REFERENCES lab(id) ON DELETE SET NULL,
    doctor_id UUID REFERENCES doctor_profile(id) ON DELETE SET NULL,
    patient_id UUID REFERENCES patient_profile(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

CREATE TABLE lab_result (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    result DOUBLE PRECISION,
    unit VARCHAR(255),
    status VARCHAR(255),
    description VARCHAR(255),
    normal_result VARCHAR(255),
    lab_test_id UUID REFERENCES lab_test(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- =============================================
-- GROUP TABLE
-- =============================================

CREATE TABLE groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description VARCHAR(255),
    icon_public_id VARCHAR(255),
    icon_url VARCHAR(255),
    color VARCHAR(50),
    patient_id UUID NOT NULL REFERENCES patient_profile(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- =============================================
-- MEDICINE TABLE
-- =============================================

CREATE TABLE medicine (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
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
    drug_id UUID NOT NULL REFERENCES drug(id),
    doctor_id UUID REFERENCES doctor_profile(id) ON DELETE SET NULL,
    patient_id UUID NOT NULL REFERENCES patient_profile(id) ON DELETE CASCADE,
    group_id UUID REFERENCES groups(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

CREATE INDEX patient_medicine_idx ON medicine(patient_id);
CREATE INDEX doctor_medicine_idx ON medicine(doctor_id);

-- Trigram index for partial/fuzzy search on medicine name
CREATE INDEX medicine_name_trgm_idx ON medicine USING GIN(name gin_trgm_ops);

-- Custom Days collection table for Medicine
CREATE TABLE custom_days (
    reminder_id UUID NOT NULL REFERENCES medicine(id) ON DELETE CASCADE,
    day VARCHAR(50)
);

-- Full-text search vector column for Medicine
ALTER TABLE medicine ADD COLUMN search_vector tsvector;

CREATE INDEX medicine_search_vector_idx ON medicine USING GIN(search_vector);

-- Function to update search vector
CREATE OR REPLACE FUNCTION medicine_search_vector_update() RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', COALESCE(NEW.name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.notes, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.dosage, '')), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to automatically update the search vector
CREATE TRIGGER medicine_search_vector_trigger
    BEFORE INSERT OR UPDATE OF name, notes, dosage
    ON medicine
    FOR EACH ROW
    EXECUTE FUNCTION medicine_search_vector_update();

-- =============================================
-- REMINDER TABLE
-- =============================================

CREATE TABLE reminder (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vibrate BOOLEAN,
    status VARCHAR(50),
    next_reminder TIMESTAMP WITHOUT TIME ZONE,
    disable BOOLEAN DEFAULT FALSE,
    medicine_id UUID NOT NULL UNIQUE REFERENCES medicine(id) ON DELETE CASCADE,
    patient_id UUID NOT NULL REFERENCES patient_profile(id) ON DELETE CASCADE,
    group_id UUID REFERENCES groups(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

CREATE INDEX patient_idx ON reminder(patient_id);
CREATE INDEX medicine_idx ON reminder(medicine_id);

-- =============================================
-- REMINDER LOG TABLE
-- =============================================

CREATE TABLE reminder_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status VARCHAR(50),
    timestamp TIMESTAMP WITHOUT TIME ZONE,
    reminder_id UUID NOT NULL REFERENCES reminder(id) ON DELETE CASCADE,
    patient_id UUID NOT NULL REFERENCES patient_profile(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID
);

