CREATE TABLE address (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    street VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    country VARCHAR(255),
    postal_code VARCHAR(255),
    user_id UUID,
    lab_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_address_lab FOREIGN KEY (lab_id) REFERENCES lab (id)
);
