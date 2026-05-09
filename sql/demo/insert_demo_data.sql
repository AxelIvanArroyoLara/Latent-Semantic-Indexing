-- Demo data for the LSI document base persistence module.
-- This file is NOT a Flyway migration.
-- Run it manually with:
-- psql -U postgres -h localhost -p 5433 -d lsi_documentbase -f sql/demo/insert_demo_data.sql

BEGIN;

-- Clean demo-related transactional tables first.
-- Linguistic resources from V6-V8 are preserved.
DELETE FROM query_results;
DELETE FROM query_runs;
DELETE FROM latent_query_vectors;
DELETE FROM latent_document_vectors;
DELETE FROM latent_models;
DELETE FROM document_terms;
DELETE FROM documents;
DELETE FROM terms;

-- Insert demo documents.
INSERT INTO documents (code, title, source, raw_text, normalized_text, language_code)
VALUES
(
    'D1',
    'Academic Stress in University Students',
    'data/raw/D1.txt',
    'Academic stress affects university students and can influence wellbeing, sleep, and performance.',
    'academic stress affects university students influence wellbeing sleep performance',
    'en'
),
(
    'D2',
    'Anxiety and School Performance',
    'data/raw/D2.txt',
    'Anxiety and worry can reduce concentration and affect school performance.',
    'anxiety worry reduce concentration affect school performance',
    'en'
),
(
    'D3',
    'Sleep Quality and Mental Health',
    'data/raw/D3.txt',
    'Sleep quality is related to mental health, emotional regulation, and academic performance.',
    'sleep quality related mental health emotional regulation academic performance',
    'en'
);

-- Insert demo terms.
INSERT INTO terms (normalized_term, canonical_term, stem, sense_label)
VALUES
('stress', 'stress', 'stress', NULL),
('anxiety', 'anxiety', 'anxieti', NULL),
('sleep', 'sleep', 'sleep', NULL),
('performance', 'performance', 'perform', 'academic_performance'),
('wellbeing', 'wellbeing', 'wellbeing', NULL),
('support', 'support', 'support', 'institutional_support');

-- Insert demo frequency matrix values.
-- This represents FrecT in relational form.
INSERT INTO document_terms (document_id, term_id, raw_frequency, weighted_frequency)
SELECT d.document_id, t.term_id, 3, 0.90
FROM documents d, terms t
WHERE d.code = 'D1' AND t.normalized_term = 'stress';

INSERT INTO document_terms (document_id, term_id, raw_frequency, weighted_frequency)
SELECT d.document_id, t.term_id, 1, 0.40
FROM documents d, terms t
WHERE d.code = 'D1' AND t.normalized_term = 'wellbeing';

INSERT INTO document_terms (document_id, term_id, raw_frequency, weighted_frequency)
SELECT d.document_id, t.term_id, 1, 0.30
FROM documents d, terms t
WHERE d.code = 'D1' AND t.normalized_term = 'performance';

INSERT INTO document_terms (document_id, term_id, raw_frequency, weighted_frequency)
SELECT d.document_id, t.term_id, 3, 0.95
FROM documents d, terms t
WHERE d.code = 'D2' AND t.normalized_term = 'anxiety';

INSERT INTO document_terms (document_id, term_id, raw_frequency, weighted_frequency)
SELECT d.document_id, t.term_id, 2, 0.70
FROM documents d, terms t
WHERE d.code = 'D2' AND t.normalized_term = 'performance';

INSERT INTO document_terms (document_id, term_id, raw_frequency, weighted_frequency)
SELECT d.document_id, t.term_id, 1, 0.35
FROM documents d, terms t
WHERE d.code = 'D2' AND t.normalized_term = 'stress';

INSERT INTO document_terms (document_id, term_id, raw_frequency, weighted_frequency)
SELECT d.document_id, t.term_id, 3, 0.92
FROM documents d, terms t
WHERE d.code = 'D3' AND t.normalized_term = 'sleep';

INSERT INTO document_terms (document_id, term_id, raw_frequency, weighted_frequency)
SELECT d.document_id, t.term_id, 2, 0.75
FROM documents d, terms t
WHERE d.code = 'D3' AND t.normalized_term = 'wellbeing';

INSERT INTO document_terms (document_id, term_id, raw_frequency, weighted_frequency)
SELECT d.document_id, t.term_id, 1, 0.45
FROM documents d, terms t
WHERE d.code = 'D3' AND t.normalized_term = 'performance';

-- Insert one demo LSI model.
INSERT INTO latent_models (k_value, source_matrix_type, notes)
VALUES
(2, 'raw_frequency', 'Demo LSI model with k=2 for persistence verification');

-- Insert document vectors for the latest model.
INSERT INTO latent_document_vectors (latent_model_id, document_id, component_index, component_value)
SELECT lm.latent_model_id, d.document_id, 0, 0.82
FROM latent_models lm, documents d
WHERE lm.notes = 'Demo LSI model with k=2 for persistence verification'
  AND d.code = 'D1';

INSERT INTO latent_document_vectors (latent_model_id, document_id, component_index, component_value)
SELECT lm.latent_model_id, d.document_id, 1, 0.31
FROM latent_models lm, documents d
WHERE lm.notes = 'Demo LSI model with k=2 for persistence verification'
  AND d.code = 'D1';

INSERT INTO latent_document_vectors (latent_model_id, document_id, component_index, component_value)
SELECT lm.latent_model_id, d.document_id, 0, 0.76
FROM latent_models lm, documents d
WHERE lm.notes = 'Demo LSI model with k=2 for persistence verification'
  AND d.code = 'D2';

INSERT INTO latent_document_vectors (latent_model_id, document_id, component_index, component_value)
SELECT lm.latent_model_id, d.document_id, 1, 0.48
FROM latent_models lm, documents d
WHERE lm.notes = 'Demo LSI model with k=2 for persistence verification'
  AND d.code = 'D2';

INSERT INTO latent_document_vectors (latent_model_id, document_id, component_index, component_value)
SELECT lm.latent_model_id, d.document_id, 0, 0.28
FROM latent_models lm, documents d
WHERE lm.notes = 'Demo LSI model with k=2 for persistence verification'
  AND d.code = 'D3';

INSERT INTO latent_document_vectors (latent_model_id, document_id, component_index, component_value)
SELECT lm.latent_model_id, d.document_id, 1, 0.91
FROM latent_models lm, documents d
WHERE lm.notes = 'Demo LSI model with k=2 for persistence verification'
  AND d.code = 'D3';

-- Insert query vector components.
INSERT INTO latent_query_vectors (latent_model_id, query_text, component_index, component_value)
SELECT lm.latent_model_id, 'academic stress anxiety', 0, 0.79
FROM latent_models lm
WHERE lm.notes = 'Demo LSI model with k=2 for persistence verification';

INSERT INTO latent_query_vectors (latent_model_id, query_text, component_index, component_value)
SELECT lm.latent_model_id, 'academic stress anxiety', 1, 0.38
FROM latent_models lm
WHERE lm.notes = 'Demo LSI model with k=2 for persistence verification';

-- Insert query run.
INSERT INTO query_runs (query_text, similarity_metric, n_value)
VALUES
('academic stress anxiety', 'cosine', 3);

-- Insert ranked query results.
INSERT INTO query_results (query_run_id, rank_position, document_id, score)
SELECT qr.query_run_id, 1, d.document_id, 0.95
FROM query_runs qr, documents d
WHERE qr.query_text = 'academic stress anxiety'
  AND qr.similarity_metric = 'cosine'
  AND d.code = 'D1';

INSERT INTO query_results (query_run_id, rank_position, document_id, score)
SELECT qr.query_run_id, 2, d.document_id, 0.88
FROM query_runs qr, documents d
WHERE qr.query_text = 'academic stress anxiety'
  AND qr.similarity_metric = 'cosine'
  AND d.code = 'D2';

INSERT INTO query_results (query_run_id, rank_position, document_id, score)
SELECT qr.query_run_id, 3, d.document_id, 0.41
FROM query_runs qr, documents d
WHERE qr.query_text = 'academic stress anxiety'
  AND qr.similarity_metric = 'cosine'
  AND d.code = 'D3';

COMMIT;