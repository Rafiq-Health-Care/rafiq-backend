-- Create lab_test table
CREATE TABLE lab_test (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description TEXT,
    code VARCHAR(255),
    pdf VARCHAR(255),
    public_id VARCHAR(255),
    file_type VARCHAR(255),
    date TIMESTAMP,
    lab_id UUID,
    doctor_id UUID,
    patient_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID,
    CONSTRAINT fk_lab_test_lab FOREIGN KEY (lab_id) REFERENCES lab(id),
    CONSTRAINT fk_lab_test_doctor FOREIGN KEY (doctor_id) REFERENCES doctor_profile(id),
    CONSTRAINT fk_lab_test_patient FOREIGN KEY (patient_id) REFERENCES patient_profile(id)
);

-- Create lab_result table
CREATE TABLE lab_result (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    result DOUBLE PRECISION,
    unit VARCHAR(255),
    status VARCHAR(255),
    description TEXT,
    normal_result VARCHAR(255),
    lab_test_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID,
    CONSTRAINT fk_lab_result_lab_test FOREIGN KEY (lab_test_id) REFERENCES lab_test(id) ON DELETE CASCADE
);

-- Create medical_certifications table
CREATE TABLE medical_certifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description TEXT,
    code VARCHAR(255),
    photo VARCHAR(255),
    doctor_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID,
    CONSTRAINT fk_medical_certifications_doctor FOREIGN KEY (doctor_id) REFERENCES doctor_profile(id) ON DELETE CASCADE
);


