-- Create a drug table to store Egyptian medicine data
CREATE TABLE drug (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trade_name VARCHAR(255) NOT NULL,
    drug_group VARCHAR(255),
    dosage_form VARCHAR(255),
    route VARCHAR(255),
    price NUMERIC(10, 2),
    pharmacology TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- Create a company table
CREATE TABLE company (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    country VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- Create an active ingredient table
CREATE TABLE active_ingredient (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- Create join table between drug and company (many-to-many)
CREATE TABLE drug_company (
    drug_id UUID NOT NULL,
    company_id UUID NOT NULL,
    PRIMARY KEY (drug_id, company_id),
    CONSTRAINT fk_drug_company_drug FOREIGN KEY (drug_id) REFERENCES drug(id) ON DELETE CASCADE,
    CONSTRAINT fk_drug_company_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE
);

-- Create a join table between drug and active ingredient (many-to-many)
CREATE TABLE drug_active_ingredient (
    drug_id UUID NOT NULL,
    active_ingredient_id UUID NOT NULL,
    PRIMARY KEY (drug_id, active_ingredient_id),
    CONSTRAINT fk_drug_active_ingredient_drug FOREIGN KEY (drug_id) REFERENCES drug(id) ON DELETE CASCADE,
    CONSTRAINT fk_drug_active_ingredient_ai FOREIGN KEY (active_ingredient_id) REFERENCES active_ingredient(id) ON DELETE CASCADE
);

-- Useful indexes for querying by trade name and data identifiers
CREATE INDEX idx_drug_trade_name ON drug(trade_name);
CREATE INDEX idx_drug_external_id ON drug(id);
CREATE INDEX idx_company_name ON company(name);
CREATE INDEX idx_active_ingredient_name ON active_ingredient(name);

ALTER TABLE drug ADD COLUMN search_vector TSVECTOR GENERATED ALWAYS AS (to_tsvector('english', trade_name)) STORED;
CREATE INDEX idx_drug_search ON drug USING GIN(search_vector);
