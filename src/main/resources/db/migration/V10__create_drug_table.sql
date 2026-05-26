CREATE TABLE IF NOT EXISTS drug (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trade_name VARCHAR(255),
    drug_group VARCHAR(255),
    dosage_form VARCHAR(255),
    route VARCHAR(255),
    pharmacology TEXT,
    price DOUBLE PRECISION NOT NULL DEFAULT 0,
    search_vector TSVECTOR,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_drug_search_vector ON drug USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS idx_drug_trade_name_trgm ON drug USING GIN (trade_name gin_trgm_ops);
