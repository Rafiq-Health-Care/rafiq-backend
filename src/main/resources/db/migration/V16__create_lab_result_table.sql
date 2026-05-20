CREATE TABLE lab_result (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    result DOUBLE PRECISION NOT NULL DEFAULT 0,
    unit VARCHAR(255),
    status VARCHAR(255),
    description VARCHAR(255),
    normal_result VARCHAR(255),
    lab_test_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by VARCHAR(255),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_lab_result_lab_test FOREIGN KEY (lab_test_id) REFERENCES lab_test (id)
);
