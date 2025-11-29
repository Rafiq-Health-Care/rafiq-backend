-- =============================================
-- V3: Insert Default Roles and Specializations
-- =============================================

-- Insert roles: ROLE_DOCTOR, ROLE_PATIENT, ROLE_USER, ROLE_ADMIN
DO $$
DECLARE
    system_user_id UUID := '00000000-0000-0000-0000-000000000001';
BEGIN
    -- Insert ROLE_DOCTOR
    IF NOT EXISTS (SELECT 1 FROM role WHERE name = 'ROLE_DOCTOR') THEN
        INSERT INTO role (id, name, created_at, created_by)
        VALUES (gen_random_uuid(), 'ROLE_DOCTOR', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- Insert ROLE_PATIENT
    IF NOT EXISTS (SELECT 1 FROM role WHERE name = 'ROLE_PATIENT') THEN
        INSERT INTO role (id, name, created_at, created_by)
        VALUES (gen_random_uuid(), 'ROLE_PATIENT', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- Insert ROLE_USER
    IF NOT EXISTS (SELECT 1 FROM role WHERE name = 'ROLE_USER') THEN
        INSERT INTO role (id, name, created_at, created_by)
        VALUES (gen_random_uuid(), 'ROLE_USER', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- Insert ROLE_ADMIN
    IF NOT EXISTS (SELECT 1 FROM role WHERE name = 'ROLE_ADMIN') THEN
        INSERT INTO role (id, name, created_at, created_by)
        VALUES (gen_random_uuid(), 'ROLE_ADMIN', CURRENT_TIMESTAMP, system_user_id);
    END IF;
END $$;

-- Insert specializations
DO $$
DECLARE
    system_user_id UUID := '00000000-0000-0000-0000-000000000001';
BEGIN
    -- Cardiology
    IF NOT EXISTS (SELECT 1 FROM specialization WHERE name = 'Cardiology' OR code = 'CARD') THEN
        INSERT INTO specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Cardiology', 'Cardiovascular diseases and heart conditions', 'CARD', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- Neurology
    IF NOT EXISTS (SELECT 1 FROM specialization WHERE name = 'Neurology' OR code = 'NEURO') THEN
        INSERT INTO specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Neurology', 'Nervous system disorders and brain conditions', 'NEURO', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- Pediatrics
    IF NOT EXISTS (SELECT 1 FROM specialization WHERE name = 'Pediatrics' OR code = 'PED') THEN
        INSERT INTO specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Pediatrics', 'Medical care for infants, children, and adolescents', 'PED', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- General Medicine
    IF NOT EXISTS (SELECT 1 FROM specialization WHERE name = 'General Medicine' OR code = 'GEN') THEN
        INSERT INTO specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'General Medicine', 'General medical practice and primary care', 'GEN', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- Orthopedics
    IF NOT EXISTS (SELECT 1 FROM specialization WHERE name = 'Orthopedics' OR code = 'ORTHO') THEN
        INSERT INTO specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Orthopedics', 'Musculoskeletal system, bones, joints, and muscles', 'ORTHO', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- Dermatology
    IF NOT EXISTS (SELECT 1 FROM specialization WHERE name = 'Dermatology' OR code = 'DERM') THEN
        INSERT INTO specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Dermatology', 'Skin, hair, and nail conditions', 'DERM', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- Ophthalmology
    IF NOT EXISTS (SELECT 1 FROM specialization WHERE name = 'Ophthalmology' OR code = 'OPHTH') THEN
        INSERT INTO specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Ophthalmology', 'Eye and vision care', 'OPHTH', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- ENT
    IF NOT EXISTS (SELECT 1 FROM specialization WHERE name = 'ENT' OR code = 'ENT') THEN
        INSERT INTO specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'ENT', 'Ear, nose, and throat conditions', 'ENT', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- Psychiatry
    IF NOT EXISTS (SELECT 1 FROM specialization WHERE name = 'Psychiatry' OR code = 'PSYCH') THEN
        INSERT INTO specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Psychiatry', 'Mental health and psychiatric disorders', 'PSYCH', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- Gynecology
    IF NOT EXISTS (SELECT 1 FROM specialization WHERE name = 'Gynecology' OR code = 'GYN') THEN
        INSERT INTO specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Gynecology', 'Women''s reproductive health', 'GYN', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- Urology
    IF NOT EXISTS (SELECT 1 FROM specialization WHERE name = 'Urology' OR code = 'URO') THEN
        INSERT INTO specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Urology', 'Urinary tract and male reproductive system', 'URO', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- Oncology
    IF NOT EXISTS (SELECT 1 FROM specialization WHERE name = 'Oncology' OR code = 'ONCO') THEN
        INSERT INTO specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Oncology', 'Cancer diagnosis and treatment', 'ONCO', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- Gastroenterology
    IF NOT EXISTS (SELECT 1 FROM specialization WHERE name = 'Gastroenterology' OR code = 'GI') THEN
        INSERT INTO specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Gastroenterology', 'Digestive system disorders', 'GI', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- Pulmonology
    IF NOT EXISTS (SELECT 1 FROM specialization WHERE name = 'Pulmonology' OR code = 'PULM') THEN
        INSERT INTO specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Pulmonology', 'Respiratory system and lung conditions', 'PULM', CURRENT_TIMESTAMP, system_user_id);
    END IF;

    -- Endocrinology
    IF NOT EXISTS (SELECT 1 FROM specialization WHERE name = 'Endocrinology' OR code = 'ENDO') THEN
        INSERT INTO specialization (id, name, description, code, created_at, created_by)
        VALUES (gen_random_uuid(), 'Endocrinology', 'Hormone and metabolic disorders', 'ENDO', CURRENT_TIMESTAMP, system_user_id);
    END IF;
END $$;

