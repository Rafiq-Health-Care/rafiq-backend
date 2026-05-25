CREATE TABLE IF NOT EXISTS drug_active_ingredient (
    drug_id UUID NOT NULL,
    active_ingredient_id UUID NOT NULL,
    CONSTRAINT pk_drug_active_ingredient PRIMARY KEY (drug_id, active_ingredient_id),
    CONSTRAINT fk_drug_active_ingredient_drug FOREIGN KEY (drug_id) REFERENCES drug (id) ON DELETE CASCADE,
    CONSTRAINT fk_drug_active_ingredient_active_ingredient FOREIGN KEY (active_ingredient_id) REFERENCES active_ingredient (id) ON DELETE CASCADE
);