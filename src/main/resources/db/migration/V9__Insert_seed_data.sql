-- Insert default roles
DO $$
DECLARE
    system_user_id UUID := '00000000-0000-0000-0000-000000000001';
BEGIN
    IF NOT EXISTS (SELECT 1 FROM user_schema.role WHERE name = 'ROLE_DOCTOR') THEN
        INSERT INTO user_schema.role (id, name, created_at, created_by)
        VALUES (gen_random_uuid(), 'ROLE_DOCTOR', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM user_schema.role WHERE name = 'ROLE_PATIENT') THEN
        INSERT INTO user_schema.role (id, name, created_at, created_by)
        VALUES (gen_random_uuid(), 'ROLE_PATIENT', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM user_schema.role WHERE name = 'ROLE_USER') THEN
        INSERT INTO user_schema.role (id, name, created_at, created_by)
        VALUES (gen_random_uuid(), 'ROLE_USER', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM user_schema.role WHERE name = 'ROLE_ADMIN') THEN
        INSERT INTO user_schema.role (id, name, created_at, created_by)
        VALUES (gen_random_uuid(), 'ROLE_ADMIN', CURRENT_TIMESTAMP, system_user_id);
    END IF;
END $$;

-- Insert default specializations
DO $$
DECLARE
    system_user_id UUID := '00000000-0000-0000-0000-000000000001';
BEGIN
    IF NOT EXISTS (SELECT 1 FROM doctor_schema.specialization WHERE name = 'Cardiology') THEN
        INSERT INTO doctor_schema.specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Cardiology', 'Cardiovascular diseases and heart conditions', 'CARD', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM doctor_schema.specialization WHERE name = 'Neurology') THEN
        INSERT INTO doctor_schema.specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Neurology', 'Nervous system disorders and brain conditions', 'NEURO', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM doctor_schema.specialization WHERE name = 'Pediatrics') THEN
        INSERT INTO doctor_schema.specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Pediatrics', 'Medical care for infants, children, and adolescents', 'PED', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM doctor_schema.specialization WHERE name = 'General Medicine') THEN
        INSERT INTO doctor_schema.specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'General Medicine', 'General medical practice and primary care', 'GEN', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM doctor_schema.specialization WHERE name = 'Orthopedics') THEN
        INSERT INTO doctor_schema.specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Orthopedics', 'Musculoskeletal system, bones, joints, and muscles', 'ORTHO', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM doctor_schema.specialization WHERE name = 'Dermatology') THEN
        INSERT INTO doctor_schema.specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Dermatology', 'Skin, hair, and nail conditions', 'DERM', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM doctor_schema.specialization WHERE name = 'Ophthalmology') THEN
        INSERT INTO doctor_schema.specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Ophthalmology', 'Eye and vision care', 'OPHTH', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM doctor_schema.specialization WHERE name = 'ENT') THEN
        INSERT INTO doctor_schema.specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'ENT', 'Ear, nose, and throat conditions', 'ENT', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM doctor_schema.specialization WHERE name = 'Psychiatry') THEN
        INSERT INTO doctor_schema.specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Psychiatry', 'Mental health and psychiatric disorders', 'PSYCH', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM doctor_schema.specialization WHERE name = 'Gynecology') THEN
        INSERT INTO doctor_schema.specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Gynecology', 'Women''s reproductive health', 'GYN', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM doctor_schema.specialization WHERE name = 'Urology') THEN
        INSERT INTO doctor_schema.specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Urology', 'Urinary tract and male reproductive system', 'URO', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM doctor_schema.specialization WHERE name = 'Oncology') THEN
        INSERT INTO doctor_schema.specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Oncology', 'Cancer diagnosis and treatment', 'ONCO', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM doctor_schema.specialization WHERE name = 'Gastroenterology') THEN
        INSERT INTO doctor_schema.specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Gastroenterology', 'Digestive system disorders', 'GI', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM doctor_schema.specialization WHERE name = 'Pulmonology') THEN
        INSERT INTO doctor_schema.specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Pulmonology', 'Respiratory system and lung conditions', 'PULM', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM doctor_schema.specialization WHERE name = 'Endocrinology') THEN
        INSERT INTO doctor_schema.specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Endocrinology', 'Hormone and metabolic disorders', 'ENDO', CURRENT_TIMESTAMP, system_user_id);
    END IF;
END $$;
