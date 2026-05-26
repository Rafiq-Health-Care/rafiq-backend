CREATE TABLE IF NOT EXISTS token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(1000) NOT NULL,
    token_type VARCHAR(255),
    expiry_date TIMESTAMP,
    user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uq_token_token UNIQUE (token),
    CONSTRAINT chk_token_type CHECK (
        token_type IN ('OTP', 'REFRESH', 'ACCESS_TOKEN', 'JWT_BLACKLIST') OR token_type IS NULL
    ),
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_token_token ON token (token);
CREATE INDEX IF NOT EXISTS idx_token_user ON token (user_id);
