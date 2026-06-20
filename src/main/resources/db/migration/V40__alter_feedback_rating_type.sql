ALTER TABLE feedback
    ALTER COLUMN rating TYPE DOUBLE PRECISION USING rating::double precision;
