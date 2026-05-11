-- Shows the relational representation of FrecT.
-- Each row represents the frequency of one term in one document.

SELECT
    d.code AS document_code,
    d.title AS document_title,
    t.normalized_term,
    t.canonical_term,
    t.stem,
    t.sense_label,
    dt.raw_frequency,
    dt.weighted_frequency
FROM document_terms dt
JOIN documents d
    ON dt.document_id = d.document_id
JOIN terms t
    ON dt.term_id = t.term_id
ORDER BY
    d.code,
    t.normalized_term;