-- Lab table (has @Embedded SocialLinks)
CREATE TABLE lab_schema.lab (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    logo_url VARCHAR(255),
    logo_id VARCHAR(255),
    facebook VARCHAR(255),
    twitter VARCHAR(255),
    instagram VARCHAR(255),
    linkedin VARCHAR(255),
    youtube VARCHAR(255),
    whatsapp VARCHAR(255),
    website VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);
