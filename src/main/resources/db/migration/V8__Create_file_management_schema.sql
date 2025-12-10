-- File Metadata table
CREATE TABLE file_managment_schema.file_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_name VARCHAR(255),
    public_id VARCHAR(255),
    file_url VARCHAR(500),
    file_size BIGINT,
    mime_type VARCHAR(100),
    category VARCHAR(100),
    owner_id UUID,
    user_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);
