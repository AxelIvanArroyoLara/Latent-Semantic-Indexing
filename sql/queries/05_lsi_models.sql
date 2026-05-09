-- Lists all LSI/SVD models that have been persisted.

SELECT
    latent_model_id,
    k_value,
    source_matrix_type,
    created_at,
    notes
FROM latent_models
ORDER BY latent_model_id;