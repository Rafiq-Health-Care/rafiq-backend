CREATE TABLE token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(1000) NOT NULL,
    token_type VARCHAR(255),
    expiry_date TIMESTAMPTZ,
    user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uq_token_token UNIQUE (token),
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX token_idx ON token (token);
CREATE INDEX user_idx ON token (user_id);
