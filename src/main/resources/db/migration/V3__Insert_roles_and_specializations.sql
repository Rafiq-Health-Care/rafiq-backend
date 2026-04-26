-- =============================================
-- V3: Insert Default Roles
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
