CREATE TABLE IF NOT EXISTS company (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    country VARCHAR(255),
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ
);
