-- Lists the vocabulary terms stored in the system.

SELECT
    term_id,
    normalized_term,
    canonical_term,
    stem,
    sense_label
FROM terms
ORDER BY term_id;