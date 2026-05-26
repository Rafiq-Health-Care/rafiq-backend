CREATE OR REPLACE VIEW doctor_search_view AS
SELECT
    d.id AS doctor_id,
    d.personal_photo AS personal_photo,
    u.first_name AS first_name,
    u.last_name AS last_name,
    d.specialization AS specialization,
    d.price AS price,
    d.rating AS rating,
    d.experience_years AS experience_years,
    u.gender AS gender,
    MIN(cs.start_time) AS next_available
FROM doctor d
    JOIN users u ON d.id = u.id
    JOIN consultation_slot cs ON cs.doctor_id = d.id
WHERE cs.start_time >= NOW()
    AND cs.deleted = FALSE
GROUP BY
    d.id,
    d.personal_photo,
    u.first_name,
    u.last_name,
    d.specialization,
    d.price,
    d.rating,
    d.experience_years,
    u.gender;
