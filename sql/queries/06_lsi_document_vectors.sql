-- Shows the latent vectors associated with each document and LSI model.

SELECT
    lm.latent_model_id,
    lm.k_value,
    d.code AS document_code,
    d.title AS document_title,
    ldv.component_index,
    ldv.component_value
FROM latent_document_vectors ldv
JOIN latent_models lm
    ON ldv.latent_model_id = lm.latent_model_id
JOIN documents d
    ON ldv.document_id = d.document_id
ORDER BY
    lm.latent_model_id,
    d.code,
    ldv.component_index;