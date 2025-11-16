-- Create users table
-- Note: Foreign keys to profiles will be added in V3 after profile tables are created
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone VARCHAR(255),
    age INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT false,
    locked BOOLEAN NOT NULL DEFAULT false,
    enabled BOOLEAN NOT NULL DEFAULT false,
    gender VARCHAR(20),
    doctor_profile_id UUID,
    patient_profile_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID
);

-- Create user_roles join table
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

