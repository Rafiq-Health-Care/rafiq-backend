ALTER TABLE consultation ADD COLUMN specialization VARCHAR(255);
CREATE INDEX consultation_specialization_idx ON consultation (specialization);