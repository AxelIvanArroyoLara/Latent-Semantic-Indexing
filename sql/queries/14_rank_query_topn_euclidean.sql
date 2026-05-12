-- Ranks documents for a query using Euclidean distance in SQL.
-- Query terms are declared in query_terms; n is declared in params.

WITH params AS (
    SELECT 5::INTEGER AS n
),
query_terms(term) AS (
    VALUES
        ('academic'),
        ('stress'),
        ('anxiety')
),
all_terms AS (
    SELECT term_id FROM terms
),
query_vector AS (
    SELECT
        t.term_id,
        COUNT(*)::DOUBLE PRECISION AS weight
    FROM query_terms qt
    JOIN terms t ON t.normalized_term = qt.term OR t.canonical_term = qt.term
    GROUP BY t.term_id
),
complete_document_vectors AS (
    SELECT
        d.document_id,
        d.code,
        d.title,
        at.term_id,
        COALESCE(dt.weighted_frequency, 0.0) AS document_weight,
        COALESCE(qv.weight, 0.0) AS query_weight
    FROM documents d
    CROSS JOIN all_terms at
    LEFT JOIN document_terms dt
        ON dt.document_id = d.document_id
       AND dt.term_id = at.term_id
    LEFT JOIN query_vector qv
        ON qv.term_id = at.term_id
)
SELECT
    code,
    title,
    SQRT(SUM(POWER(document_weight - query_weight, 2))) AS euclidean_distance
FROM complete_document_vectors
GROUP BY document_id, code, title
ORDER BY euclidean_distance ASC, code
LIMIT (SELECT n FROM params);
