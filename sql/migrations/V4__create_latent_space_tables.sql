CREATE TABLE IF NOT EXISTS latent_models (
    latent_model_id BIGSERIAL PRIMARY KEY,
    k_value INTEGER NOT NULL,
    source_matrix_type VARCHAR(50) NOT NULL DEFAULT 'raw_frequency',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    CHECK (k_value > 0),
    CHECK (source_matrix_type IN ('raw_frequency', 'weighted_frequency'))
);

CREATE TABLE IF NOT EXISTS latent_document_vectors (
    latent_model_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    component_index INTEGER NOT NULL,
    component_value DOUBLE PRECISION NOT NULL,

    PRIMARY KEY (latent_model_id, document_id, component_index),

    CONSTRAINT fk_latent_document_vectors_model
        FOREIGN KEY (latent_model_id)
        REFERENCES latent_models(latent_model_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_latent_document_vectors_document
        FOREIGN KEY (document_id)
        REFERENCES documents(document_id)
        ON DELETE CASCADE,

    CHECK (component_index >= 0)
);

CREATE TABLE IF NOT EXISTS latent_query_vectors (
    latent_query_vector_id BIGSERIAL PRIMARY KEY,
    latent_model_id BIGINT NOT NULL,
    query_text TEXT NOT NULL,
    component_index INTEGER NOT NULL,
    component_value DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_latent_query_vectors_model
        FOREIGN KEY (latent_model_id)
        REFERENCES latent_models(latent_model_id)
        ON DELETE CASCADE,

    CHECK (component_index >= 0)
);