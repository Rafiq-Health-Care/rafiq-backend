-- =============================================================================
-- V3 · Seed data
-- Inserts mandatory reference data required for the application to function.
-- Uses ON CONFLICT DO NOTHING so this script is safe to re-run.
-- =============================================================================

-- System actor UUID used as created_by for all seed rows
-- (no FK constraint on created_by, so no users row is required)
DO $$
DECLARE
    system_id UUID := '00000000-0000-0000-0000-000000000001';
BEGIN
    -- -------------------------------------------------------------------------
    -- Roles  (matches Roles enum: ROLE_DOCTOR, ROLE_PATIENT, ROLE_USER, ROLE_ADMIN)
    -- -------------------------------------------------------------------------
    INSERT INTO role (id, name, created_at, created_by, deleted)
    VALUES
        ('10000000-0000-0000-0000-000000000001', 'ROLE_DOCTOR',  CURRENT_TIMESTAMP, system_id, FALSE),
        ('10000000-0000-0000-0000-000000000002', 'ROLE_PATIENT', CURRENT_TIMESTAMP, system_id, FALSE),
        ('10000000-0000-0000-0000-000000000003', 'ROLE_USER',    CURRENT_TIMESTAMP, system_id, FALSE),
        ('10000000-0000-0000-0000-000000000004', 'ROLE_ADMIN',   CURRENT_TIMESTAMP, system_id, FALSE)
    ON CONFLICT DO NOTHING;
END $$;
