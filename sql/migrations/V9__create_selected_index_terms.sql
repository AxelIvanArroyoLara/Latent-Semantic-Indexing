CREATE TABLE IF NOT EXISTS selected_index_terms (
    selected_index_term_id BIGSERIAL PRIMARY KEY,
    latent_model_id BIGINT,
    term_id BIGINT,
    term_text VARCHAR(150) NOT NULL,
    significance DOUBLE PRECISION NOT NULL,
    selected_by VARCHAR(150) NOT NULL DEFAULT 'expert',
    selection_source VARCHAR(50) NOT NULL DEFAULT 'cli',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_selected_index_terms_model
        FOREIGN KEY (latent_model_id)
        REFERENCES latent_models(latent_model_id)
        ON DELETE SET NULL,

    CONSTRAINT fk_selected_index_terms_term
        FOREIGN KEY (term_id)
        REFERENCES terms(term_id)
        ON DELETE SET NULL,

    CHECK (significance >= 0)
);
