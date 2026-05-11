CREATE TABLE IF NOT EXISTS terms (
    term_id BIGSERIAL PRIMARY KEY,
    normalized_term VARCHAR(150) NOT NULL,
    canonical_term VARCHAR(150) NOT NULL,
    stem VARCHAR(150) NOT NULL,
    sense_label VARCHAR(150),
    UNIQUE (normalized_term, sense_label)
);

CREATE TABLE IF NOT EXISTS document_terms (
    document_id BIGINT NOT NULL,
    term_id BIGINT NOT NULL,
    raw_frequency DOUBLE PRECISION NOT NULL DEFAULT 0,
    weighted_frequency DOUBLE PRECISION NOT NULL DEFAULT 0,

    PRIMARY KEY (document_id, term_id),

    CONSTRAINT fk_document_terms_document
        FOREIGN KEY (document_id)
        REFERENCES documents(document_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_document_terms_term
        FOREIGN KEY (term_id)
        REFERENCES terms(term_id)
        ON DELETE CASCADE,

    CHECK (raw_frequency >= 0),
    CHECK (weighted_frequency >= 0)
);