-- Rename group table to groups if it exists (entity uses @Table(name = "groups"))
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'group') THEN
        ALTER TABLE "group" RENAME TO groups;
    END IF;
END $$;

-- Note: name and group_id columns are now created in V9 migration
-- This migration only adds full-text search capabilities

-- Add tsvector column for full-text search on medicine name
-- Entity has: @Column(name = "search_vector", columnDefinition = "tsvector", insertable = false, updatable = false)
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS search_vector TSVECTOR 
    GENERATED ALWAYS AS (to_tsvector('english', COALESCE(name, ''))) STORED;

-- Create GIN index for full-text search
CREATE INDEX IF NOT EXISTS idx_medicine_search ON medicine USING GIN(search_vector);

-- Create trigram index for partial/fuzzy search (pg_trgm extension already enabled in V6)
CREATE INDEX IF NOT EXISTS idx_medicine_name_trgm ON medicine USING GIN (name gin_trgm_ops);

-- Update existing medicine records: set name from drug.trade_name
UPDATE medicine m
SET name = d.trade_name
FROM drug d
WHERE m.drug_id = d.id AND (m.name IS NULL OR m.name = '');

