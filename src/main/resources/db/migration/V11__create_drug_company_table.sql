CREATE TABLE IF NOT EXISTS drug_company (
    drug_id UUID NOT NULL,
    company_id UUID NOT NULL,
    CONSTRAINT pk_drug_company PRIMARY KEY (drug_id, company_id),
    CONSTRAINT fk_drug_company_drug FOREIGN KEY (drug_id) REFERENCES drug (id) ON DELETE CASCADE,
    CONSTRAINT fk_drug_company_company FOREIGN KEY (company_id) REFERENCES company (id) ON DELETE CASCADE
);
