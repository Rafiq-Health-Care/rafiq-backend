CREATE INDEX idx_drug_search_vector ON drug USING GIN (search_vector);
CREATE INDEX idx_drug_trade_name_trgm ON drug USING GIN (trade_name gin_trgm_ops);

CREATE OR REPLACE FUNCTION fn_drug_search_vector_update()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', COALESCE(NEW.trade_name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.drug_group, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.pharmacology, '')), 'C');
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_drug_search_vector
    BEFORE INSERT OR UPDATE OF trade_name, drug_group, pharmacology ON drug
    FOR EACH ROW EXECUTE FUNCTION fn_drug_search_vector_update();
