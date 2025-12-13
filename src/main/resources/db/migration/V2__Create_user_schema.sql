-- Role table
CREATE TABLE user_schema.role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Users table
CREATE TABLE user_schema.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    phone VARCHAR(255),
    birth_date DATE,
    active BOOLEAN DEFAULT TRUE,
    locked BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT FALSE,
    notification_token VARCHAR(255),
    gender VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX idx_users_email ON user_schema.users(email);

-- User-Roles join table
CREATE TABLE user_schema.user_roles (
    user_id UUID NOT NULL REFERENCES user_schema.users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES user_schema.role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Token table
CREATE TABLE user_schema.token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(1000) NOT NULL UNIQUE,
    token_type VARCHAR(50),
    expiry_date TIMESTAMP WITH TIME ZONE,
    user_id UUID NOT NULL REFERENCES user_schema.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX token_idx ON user_schema.token(token);
CREATE INDEX user_idx ON user_schema.token(user_id);

-- Address table
CREATE TABLE user_schema.address (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    street VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    country VARCHAR(255),
    postal_code VARCHAR(255),
    is_primary BOOLEAN,
    user_id UUID NOT NULL REFERENCES user_schema.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);
