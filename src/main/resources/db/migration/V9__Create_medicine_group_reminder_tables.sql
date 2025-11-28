-- Create groups table first (entity uses @Table(name = "groups"))
CREATE TABLE groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description TEXT,
    icon_public_id VARCHAR(255),
    icon_url VARCHAR(255),
    color VARCHAR(50),
    patient_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID,
    CONSTRAINT fk_group_patient FOREIGN KEY (patient_id) REFERENCES patient_profile(id)
);

CREATE INDEX idx_group_patient_id ON groups(patient_id);

-- Create a medicine table to store prescribed drugs for patients
CREATE TABLE medicine (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    dosage VARCHAR(255) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    type VARCHAR(50),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    notes TEXT,
    photo_url VARCHAR(255),
    photo_public_id VARCHAR(255),
    drug_id UUID NOT NULL,
    doctor_id UUID,
    patient_id UUID NOT NULL,
    group_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID,
    CONSTRAINT fk_medicine_drug FOREIGN KEY (drug_id) REFERENCES drug(id),
    CONSTRAINT fk_medicine_doctor FOREIGN KEY (doctor_id) REFERENCES doctor_profile(id),
    CONSTRAINT fk_medicine_patient FOREIGN KEY (patient_id) REFERENCES patient_profile(id),
    CONSTRAINT fk_medicine_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE SET NULL
);

CREATE INDEX idx_medicine_drug_id ON medicine(drug_id);
CREATE INDEX idx_medicine_doctor_id ON medicine(doctor_id);
CREATE INDEX idx_medicine_patient_id ON medicine(patient_id);
CREATE INDEX idx_medicine_group_id ON medicine(group_id);

-- Create reminder table
CREATE TABLE reminder (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hour INT NOT NULL CHECK (hour BETWEEN 0 AND 23),
    minute INT NOT NULL CHECK (minute BETWEEN 0 AND 59),
    frequency VARCHAR(50),
    vibrate BOOLEAN NOT NULL DEFAULT FALSE,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    status VARCHAR(50),
    medicine_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    group_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID,
    CONSTRAINT fk_reminder_medicine FOREIGN KEY (medicine_id) REFERENCES medicine(id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_patient FOREIGN KEY (patient_id) REFERENCES patient_profile(id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE SET NULL
);

CREATE INDEX idx_reminder_medicine_id ON reminder(medicine_id);
CREATE INDEX idx_reminder_patient_id ON reminder(patient_id);
CREATE INDEX idx_reminder_group_id ON reminder(group_id);

-- Create custom_days table for reminder's custom days (ElementCollection)
CREATE TABLE custom_days (
    reminder_id UUID NOT NULL,
    day VARCHAR(20) NOT NULL,
    CONSTRAINT fk_custom_days_reminder FOREIGN KEY (reminder_id) REFERENCES reminder(id) ON DELETE CASCADE
);

CREATE INDEX idx_custom_days_reminder_id ON custom_days(reminder_id);

-- Create a reminder_log table
CREATE TABLE reminder_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status VARCHAR(50),
    message TEXT,
    reminder_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID,
    CONSTRAINT fk_reminder_log_reminder FOREIGN KEY (reminder_id) REFERENCES reminder(id) ON DELETE CASCADE
);

CREATE INDEX idx_reminder_log_reminder_id ON reminder_log(reminder_id);

