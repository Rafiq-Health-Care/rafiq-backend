CREATE TABLE IF NOT EXISTS doctor_sub_specialization (
    doctor_id UUID NOT NULL,
    sub_specialization VARCHAR(255) NOT NULL,
    CONSTRAINT pk_doctor_sub_specialization PRIMARY KEY (doctor_id, sub_specialization),
    CONSTRAINT fk_doctor_sub_specialization_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_doctor_sub_spec ON doctor_sub_specialization (doctor_id, sub_specialization);
CREATE INDEX IF NOT EXISTS idx_sub_spec ON doctor_sub_specialization (sub_specialization);

CREATE TABLE IF NOT EXISTS doctor_languages (
    doctor_id UUID NOT NULL,
    language VARCHAR(255) NOT NULL,
    CONSTRAINT pk_doctor_languages PRIMARY KEY (doctor_id, language),
    CONSTRAINT fk_doctor_languages_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_doctor_language ON doctor_languages (doctor_id, language);
CREATE INDEX IF NOT EXISTS idx_language ON doctor_languages (language);
