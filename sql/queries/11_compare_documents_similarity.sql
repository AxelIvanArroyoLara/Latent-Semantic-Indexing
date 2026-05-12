-- Compares two documents using SQL over the relational FrecT representation.
-- Replace D1 and D2 in the params CTE to compare other documents.

WITH params AS (
    SELECT
        'D1'::VARCHAR AS first_document_code,
        'D2'::VARCHAR AS second_document_code
),
first_vector AS (
    SELECT
        t.term_id,
        dt.weighted_frequency AS weight
    FROM document_terms dt
    JOIN documents d ON d.document_id = dt.document_id
    JOIN terms t ON t.term_id = dt.term_id
    JOIN params p ON p.first_document_code = d.code
),
second_vector AS (
    SELECT
        t.term_id,
        dt.weighted_frequency AS weight
    FROM document_terms dt
    JOIN documents d ON d.document_id = dt.document_id
    JOIN terms t ON t.term_id = dt.term_id
    JOIN params p ON p.second_document_code = d.code
),
complete_vector AS (
    SELECT
        COALESCE(f.term_id, s.term_id) AS term_id,
        COALESCE(f.weight, 0.0) AS first_weight,
        COALESCE(s.weight, 0.0) AS second_weight
    FROM first_vector f
    FULL OUTER JOIN second_vector s ON s.term_id = f.term_id
),
aggregates AS (
    SELECT
        SUM(first_weight * second_weight) AS dot_product,
        SQRT(SUM(first_weight * first_weight)) AS first_norm,
        SQRT(SUM(second_weight * second_weight)) AS second_norm,
        COUNT(*) FILTER (WHERE first_weight > 0 AND second_weight > 0) AS intersection_size,
        COUNT(*) FILTER (WHERE first_weight > 0 OR second_weight > 0) AS union_size,
        SQRT(SUM(POWER(first_weight - second_weight, 2))) AS euclidean_distance
    FROM complete_vector
)
SELECT
    p.first_document_code,
    p.second_document_code,
    CASE
        WHEN a.first_norm = 0 OR a.second_norm = 0 THEN 0
        ELSE a.dot_product / (a.first_norm * a.second_norm)
    END AS cosine_similarity,
    CASE
        WHEN a.union_size = 0 THEN 0
        ELSE a.intersection_size::DOUBLE PRECISION / a.union_size
    END AS jaccard_similarity,
    a.euclidean_distance
FROM aggregates a
CROSS JOIN params p;
