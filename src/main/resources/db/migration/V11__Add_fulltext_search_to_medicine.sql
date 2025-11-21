-- Rename group table to groups if it exists (entity uses @Table(name = "groups"))
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'group') THEN
        ALTER TABLE "group" RENAME TO groups;
    END IF;
END $$;

-- Add name column to medicine table (matches entity field: private String name;)
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS name VARCHAR(255);

-- Add group_id column (changed from ManyToMany to ManyToOne)
-- Entity has: @ManyToOne @JoinColumn should be used instead of @JoinTable
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS group_id UUID;

-- Migrate data from medicine_groups join table to new group_id column
-- Note: If a medicine has multiple groups, we take the first one
UPDATE medicine m
SET group_id = (
    SELECT mg.group_id 
    FROM medicine_groups mg 
    WHERE mg.medicine_id = m.id 
    LIMIT 1
)
WHERE EXISTS (SELECT 1 FROM medicine_groups mg WHERE mg.medicine_id = m.id)
  AND m.group_id IS NULL;

-- Add foreign key constraint after data migration
-- Entity relationship: @ManyToOne(fetch = FetchType.LAZY) private Group group;
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'fk_medicine_group'
    ) THEN
        ALTER TABLE medicine ADD CONSTRAINT fk_medicine_group 
            FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Create index for group_id foreign key (for query performance)
CREATE INDEX IF NOT EXISTS idx_medicine_group_id ON medicine(group_id);

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

-- Drop the old ManyToMany join table (medicine_groups) since we now use direct foreign key
-- Entity should use @JoinColumn(name = "group_id") instead of @JoinTable
DROP TABLE IF EXISTS medicine_groups;

