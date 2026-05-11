CREATE TABLE IF NOT EXISTS query_runs (
    query_run_id BIGSERIAL PRIMARY KEY,
    query_text TEXT NOT NULL,
    similarity_metric VARCHAR(50) NOT NULL,
    n_value INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CHECK (similarity_metric IN ('cosine', 'jaccard', 'euclidean')),
    CHECK (n_value > 0)
);

CREATE TABLE IF NOT EXISTS query_results (
    query_run_id BIGINT NOT NULL,
    rank_position INTEGER NOT NULL,
    document_id BIGINT NOT NULL,
    score DOUBLE PRECISION NOT NULL,

    PRIMARY KEY (query_run_id, rank_position),

    CONSTRAINT fk_query_results_query_run
        FOREIGN KEY (query_run_id)
        REFERENCES query_runs(query_run_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_query_results_document
        FOREIGN KEY (document_id)
        REFERENCES documents(document_id)
        ON DELETE CASCADE,

    UNIQUE (query_run_id, document_id),
    CHECK (rank_position > 0)
);