-- Lab Test table
CREATE TABLE lab_test_schema.lab_test (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description VARCHAR(255),
    code VARCHAR(255),
    lab_id UUID,
    file_id UUID,
    doctor_id UUID,
    patient_id UUID,
    date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Lab Result table
CREATE TABLE lab_test_schema.lab_result (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    result DOUBLE PRECISION,
    unit VARCHAR(255),
    status VARCHAR(255),
    description VARCHAR(255),
    normal_result VARCHAR(255),
    lab_test_id UUID REFERENCES lab_test_schema.lab_test(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);
