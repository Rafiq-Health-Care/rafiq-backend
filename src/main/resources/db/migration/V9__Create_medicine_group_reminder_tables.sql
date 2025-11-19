-- Create a medicine table to store prescribed drugs for patients
CREATE TABLE medicine (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dosage VARCHAR(255),
    frequency VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    notes TEXT,
    photo_url VARCHAR(255),
    photo_public_id VARCHAR(255),
    drug_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID,
    CONSTRAINT fk_medicine_drug FOREIGN KEY (drug_id) REFERENCES drug(id),
    CONSTRAINT fk_medicine_doctor FOREIGN KEY (doctor_id) REFERENCES doctor_profile(id),
    CONSTRAINT fk_medicine_patient FOREIGN KEY (patient_id) REFERENCES patient_profile(id)
);

CREATE INDEX idx_medicine_drug_id ON medicine(drug_id);
CREATE INDEX idx_medicine_doctor_id ON medicine(doctor_id);
CREATE INDEX idx_medicine_patient_id ON medicine(patient_id);

-- Create a group table (quoted because GROUP is a reserved keyword)
CREATE TABLE "group" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
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

CREATE INDEX idx_group_patient_id ON "group"(patient_id);

-- Join the table for medicine and group many-to-many relationship
CREATE TABLE medicine_groups (
    medicine_id UUID NOT NULL,
    group_id UUID NOT NULL,
    PRIMARY KEY (medicine_id, group_id),
    CONSTRAINT fk_medicine_groups_medicine FOREIGN KEY (medicine_id) REFERENCES medicine(id) ON DELETE CASCADE,
    CONSTRAINT fk_medicine_groups_group FOREIGN KEY (group_id) REFERENCES "group"(id) ON DELETE CASCADE
);

-- Create reminder table
CREATE TABLE reminder (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hour INT NOT NULL CHECK (hour BETWEEN 0 AND 23),
    minute INT NOT NULL CHECK (minute BETWEEN 0 AND 59),
    frequency VARCHAR(50) NOT NULL,
    custom_days TEXT[] DEFAULT '{}',
    vibrate BOOLEAN NOT NULL DEFAULT FALSE,
    medicine_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    group_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID,
    CONSTRAINT fk_reminder_medicine FOREIGN KEY (medicine_id) REFERENCES medicine(id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_patient FOREIGN KEY (patient_id) REFERENCES patient_profile(id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_group FOREIGN KEY (group_id) REFERENCES "group"(id) ON DELETE SET NULL
);

CREATE INDEX idx_reminder_medicine_id ON reminder(medicine_id);
CREATE INDEX idx_reminder_patient_id ON reminder(patient_id);
CREATE INDEX idx_reminder_group_id ON reminder(group_id);

-- Create a reminder_log table
CREATE TABLE reminder_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status VARCHAR(50) NOT NULL,
    message TEXT,
    reminder_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID,
    CONSTRAINT fk_reminder_log_reminder FOREIGN KEY (reminder_id) REFERENCES reminder(id) ON DELETE CASCADE
);

CREATE INDEX idx_reminder_log_reminder_id ON reminder_log(reminder_id);

