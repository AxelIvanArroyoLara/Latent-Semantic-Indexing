-- Ranks documents for a query using Jaccard similarity in SQL.
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
    SELECT DISTINCT t.term_id
    FROM query_terms qt
    JOIN terms t ON t.normalized_term = qt.term OR t.canonical_term = qt.term
),
document_sets AS (
    SELECT
        d.document_id,
        d.code,
        d.title,
        dt.term_id
    FROM documents d
    JOIN document_terms dt ON dt.document_id = d.document_id
),
scores AS (
    SELECT
        ds.document_id,
        ds.code,
        ds.title,
        COUNT(*) FILTER (WHERE qv.term_id IS NOT NULL) AS intersection_size,
        (
            SELECT COUNT(DISTINCT term_id) FROM query_vector
        ) + COUNT(DISTINCT ds.term_id)
          - COUNT(*) FILTER (WHERE qv.term_id IS NOT NULL) AS union_size
    FROM document_sets ds
    LEFT JOIN query_vector qv ON qv.term_id = ds.term_id
    GROUP BY ds.document_id, ds.code, ds.title
)
SELECT
    code,
    title,
    CASE
        WHEN union_size = 0 THEN 0
        ELSE intersection_size::DOUBLE PRECISION / union_size
    END AS jaccard_similarity
FROM scores
ORDER BY jaccard_similarity DESC, code
LIMIT (SELECT n FROM params);
