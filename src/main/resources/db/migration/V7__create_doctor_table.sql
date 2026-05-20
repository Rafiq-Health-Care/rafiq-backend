CREATE TABLE doctor (
    id UUID PRIMARY KEY,
    description VARCHAR(255),
    hospital_name VARCHAR(255),
    personal_photo VARCHAR(255),
    national_id VARCHAR(255),
    hospital_id VARCHAR(255),
    specialization VARCHAR(255),
    social_links_id UUID,
    public_id VARCHAR(255),
    status VARCHAR(255),
    price NUMERIC(10, 2) NOT NULL DEFAULT 1000,
    biography TEXT,
    education JSONB,
    experience JSONB,
    experience_years INTEGER NOT NULL DEFAULT 0,
    rating NUMERIC(10, 2),
    balance NUMERIC(10, 2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_doctor_user FOREIGN KEY (id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_doctor_social_links FOREIGN KEY (social_links_id) REFERENCES social_links (id)
);

CREATE INDEX specialization_idx ON doctor (specialization);
