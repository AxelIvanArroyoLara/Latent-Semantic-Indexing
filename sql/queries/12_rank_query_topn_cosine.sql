-- Ranks documents for a query using cosine similarity in SQL.
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
query_vector AS (
    SELECT
        t.term_id,
        COUNT(*)::DOUBLE PRECISION AS weight
    FROM query_terms qt
    JOIN terms t ON t.normalized_term = qt.term OR t.canonical_term = qt.term
    GROUP BY t.term_id
),
document_scores AS (
    SELECT
        d.document_id,
        d.code,
        d.title,
        SUM(COALESCE(qv.weight, 0.0) * COALESCE(dt.weighted_frequency, 0.0)) AS dot_product,
        SQRT(SUM(POWER(COALESCE(qv.weight, 0.0), 2))) AS query_norm,
        SQRT(SUM(POWER(COALESCE(dt.weighted_frequency, 0.0), 2))) AS document_norm
    FROM documents d
    JOIN document_terms dt ON dt.document_id = d.document_id
    LEFT JOIN query_vector qv ON qv.term_id = dt.term_id
    GROUP BY d.document_id, d.code, d.title
)
SELECT
    code,
    title,
    CASE
        WHEN query_norm = 0 OR document_norm = 0 THEN 0
        ELSE dot_product / (query_norm * document_norm)
    END AS cosine_similarity
FROM document_scores
ORDER BY cosine_similarity DESC, code
LIMIT (SELECT n FROM params);
