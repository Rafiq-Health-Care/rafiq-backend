CREATE OR REPLACE FUNCTION fn_medicine_search_vector_update()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', COALESCE(NEW.name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.dosage, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.notes, '')), 'C');
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_medicine_search_vector
    BEFORE INSERT OR UPDATE OF name, dosage, notes ON medicine
    FOR EACH ROW EXECUTE FUNCTION fn_medicine_search_vector_update();
