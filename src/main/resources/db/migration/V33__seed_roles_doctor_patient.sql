DO $$
DECLARE
    system_id UUID := '00000000-0000-0000-0000-000000000001';
    doctor_user_id UUID := '20000000-0000-0000-0000-000000000001';
    patient_user_id UUID := '20000000-0000-0000-0000-000000000002';
    bcrypt_password VARCHAR(100) := '$2b$10$YkCtKCSkx4qLGptFgukZeO/cMfFGW9AVOBeLUTX1E1K388UBeJVWy';
BEGIN
    INSERT INTO role (id, name, created_at, created_by, deleted)
    VALUES
        ('10000000-0000-0000-0000-000000000001', 'ROLE_DOCTOR', CURRENT_TIMESTAMP, system_id, FALSE),
        ('10000000-0000-0000-0000-000000000002', 'ROLE_PATIENT', CURRENT_TIMESTAMP, system_id, FALSE),
        ('10000000-0000-0000-0000-000000000003', 'ROLE_USER', CURRENT_TIMESTAMP, system_id, FALSE),
        ('10000000-0000-0000-0000-000000000004', 'ROLE_ADMIN', CURRENT_TIMESTAMP, system_id, FALSE)
    ON CONFLICT DO NOTHING;

    INSERT INTO users (
        id,
        email,
        password,
        first_name,
        last_name,
        birth_date,
        active,
        locked,
        enabled,
        created_at,
        created_by,
        deleted
    )
    VALUES
        (
            doctor_user_id,
            'doctor.seed@rafiq.local',
            bcrypt_password,
            'Seed',
            'Doctor',
            DATE '1990-01-01',
            TRUE,
            FALSE,
            TRUE,
            CURRENT_TIMESTAMP,
            system_id,
            FALSE
        ),
        (
            patient_user_id,
            'patient.seed@rafiq.local',
            bcrypt_password,
            'Seed',
            'Patient',
            DATE '1990-01-01',
            TRUE,
            FALSE,
            TRUE,
            CURRENT_TIMESTAMP,
            system_id,
            FALSE
        )
    ON CONFLICT (email) DO NOTHING;

    INSERT INTO doctor (id)
    VALUES (doctor_user_id)
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO patient (id)
    VALUES (patient_user_id)
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO user_roles (user_id, role_id)
    VALUES
        (doctor_user_id, '10000000-0000-0000-0000-000000000001'),
        (doctor_user_id, '10000000-0000-0000-0000-000000000003'),
        (patient_user_id, '10000000-0000-0000-0000-000000000002'),
        (patient_user_id, '10000000-0000-0000-0000-000000000003')
    ON CONFLICT DO NOTHING;
END $$;