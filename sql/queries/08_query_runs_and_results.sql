-- Shows query executions and their ranked document results.

SELECT
    qr.query_run_id,
    qr.query_text,
    qr.similarity_metric,
    qr.n_value,
    qr.created_at,
    qres.rank_position,
    d.code AS document_code,
    d.title AS document_title,
    qres.score
FROM query_runs qr
LEFT JOIN query_results qres
    ON qr.query_run_id = qres.query_run_id
LEFT JOIN documents d
    ON qres.document_id = d.document_id
ORDER BY
    qr.query_run_id,
    qres.rank_position;