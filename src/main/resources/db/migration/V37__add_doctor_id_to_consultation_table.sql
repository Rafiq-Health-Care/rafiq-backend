ALTER TABLE consultation
    ADD COLUMN IF NOT EXISTS doctor_id UUID;

UPDATE consultation c
SET doctor_id = cs.doctor_id
FROM consultation_slot cs
WHERE c.slot_id = cs.id
  AND c.doctor_id IS NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM consultation WHERE doctor_id IS NULL) THEN
        ALTER TABLE consultation
            ALTER COLUMN doctor_id SET NOT NULL;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_consultation_doctor'
    ) THEN
        ALTER TABLE consultation
            ADD CONSTRAINT fk_consultation_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_consultation_doctor ON consultation (doctor_id);
