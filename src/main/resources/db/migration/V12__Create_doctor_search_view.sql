CREATE VIEW doctor_search_view AS
SELECT
    d.id                  AS doctor_id,
    d.personal_photo      AS personal_photo,
    u.first_name          AS first_name,
    u.last_name           AS last_name,
    d.specialization      AS specialization,
    d.price               AS price,
    d.rating                AS rating,
    d.experience_years    AS experience_years,
    u.gender              AS gender,
    MIN(c.start_time)     AS next_available
FROM doctor d JOIN users u ON d.id = u.id
         JOIN consultation c ON c.doctor_id = d.id
WHERE c.start_time >= NOW()
GROUP BY d.id, d.personal_photo, u.first_name, u.last_name,
         d.specialization, d.price, d.rating, d.experience_years, u.gender;