-- Shows query vectors projected into a latent model space.

SELECT
    lqv.latent_query_vector_id,
    lm.latent_model_id,
    lm.k_value,
    lqv.query_text,
    lqv.component_index,
    lqv.component_value,
    lqv.created_at
FROM latent_query_vectors lqv
JOIN latent_models lm
    ON lqv.latent_model_id = lm.latent_model_id
ORDER BY
    lm.latent_model_id,
    lqv.latent_query_vector_id,
    lqv.component_index;