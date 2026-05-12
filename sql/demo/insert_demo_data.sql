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

-- Insert the same 10-document corpus used by the Java final demo.
INSERT INTO documents (code, title, source, raw_text, normalized_text, language_code)
VALUES
('D1', 'Academic stress in university students', 'data/fixtures/query/document_terms.csv', 'academic stress student university pressure', 'academic stress student university pressure', 'en'),
('D2', 'Anxiety and school performance', 'data/fixtures/query/document_terms.csv', 'anxiety performance school student academic', 'anxiety performance school student academic', 'en'),
('D3', 'Student burnout', 'data/fixtures/query/document_terms.csv', 'burnout exhaustion student stress', 'burnout exhaustion student stress', 'en'),
('D4', 'Depression in young adults', 'data/fixtures/query/document_terms.csv', 'depression sadness young adult mental_health', 'depression sadness young adult mental_health', 'en'),
('D5', 'Sleep quality and mental health', 'data/fixtures/query/document_terms.csv', 'sleep_quality rest mental_health wellbeing', 'sleep_quality rest mental_health wellbeing', 'en'),
('D6', 'Exercise and emotional regulation', 'data/fixtures/query/document_terms.csv', 'exercise physical_activity emotional regulation mood', 'exercise physical_activity emotional regulation mood', 'en'),
('D7', 'Nutrition and concentration', 'data/fixtures/query/document_terms.csv', 'nutrition diet concentration focus student', 'nutrition diet concentration focus student', 'en'),
('D8', 'University psychological support services', 'data/fixtures/query/document_terms.csv', 'university support counseling therapy student', 'university support counseling therapy student', 'en'),
('D9', 'Social media use and anxiety', 'data/fixtures/query/document_terms.csv', 'social_media network anxiety student worry', 'social_media network anxiety student worry', 'en'),
('D10', 'Breathing techniques and self regulation', 'data/fixtures/query/document_terms.csv', 'breathing respiration self_regulation emotional regulation', 'breathing respiration self_regulation emotional regulation', 'en');

-- Insert canonical terms used by the demo corpus and SQL query examples.
INSERT INTO terms (normalized_term, canonical_term, stem, sense_label)
VALUES
('academic', 'academic', 'academic', NULL),
('academic_stress', 'academic_stress', 'academic_stress', 'academic_stress'),
('anxiety', 'anxiety', 'anxiety', NULL),
('burnout', 'burnout', 'burnout', NULL),
('concentration', 'concentration', 'concentration', NULL),
('counseling', 'counseling', 'counsel', NULL),
('depression', 'depression', 'depression', NULL),
('emotional', 'emotional', 'emotion', NULL),
('emotional_support', 'emotional_support', 'emotional_support', 'emotional_support'),
('exercise', 'physical_activity', 'exercise', NULL),
('institutional_support', 'institutional_support', 'institutional_support', 'institutional_support'),
('mental_health', 'mental_health', 'mental_health', 'mental_health'),
('nutrition', 'nutrition', 'nutrition', NULL),
('performance', 'performance', 'performance', 'academic_performance'),
('physical_activity', 'physical_activity', 'physical_activity', NULL),
('self_regulation', 'self_regulation', 'self_regulation', 'self_regulation'),
('sleep', 'sleep', 'sleep', NULL),
('sleep_quality', 'sleep_quality', 'sleep_quality', 'sleep_quality'),
('social_media', 'social_media', 'social_media', NULL),
('stress', 'stress', 'stress', NULL),
('student', 'student', 'student', NULL),
('university', 'university', 'university', NULL),
('wellbeing', 'wellbeing', 'wellbeing', NULL);

-- Insert FrecT values in relational form. These weights are deterministic
-- demo values aligned with the controlled corpus and query examples.
INSERT INTO document_terms (document_id, term_id, raw_frequency, weighted_frequency)
SELECT d.document_id, t.term_id, SUM(x.raw_frequency), SUM(x.weighted_frequency)
FROM (
    VALUES
    ('D1', 'academic_stress', 1, 2.705), ('D1', 'student', 1, 1.452), ('D1', 'university', 1, 2.299), ('D1', 'stress', 1, 1.788),
    ('D2', 'anxiety', 1, 2.299), ('D2', 'performance', 1, 2.299), ('D2', 'student', 1, 1.452), ('D2', 'academic', 1, 2.705),
    ('D3', 'burnout', 1, 2.705), ('D3', 'burnout', 1, 2.705), ('D3', 'student', 1, 1.452), ('D3', 'stress', 1, 1.788),
    ('D4', 'depression', 1, 2.705), ('D4', 'depression', 1, 2.705), ('D4', 'mental_health', 1, 2.299),
    ('D5', 'sleep_quality', 1, 2.705), ('D5', 'sleep', 1, 2.299), ('D5', 'mental_health', 1, 2.299), ('D5', 'wellbeing', 1, 2.705),
    ('D6', 'physical_activity', 1, 2.299), ('D6', 'physical_activity', 1, 2.299), ('D6', 'emotional', 1, 2.299),
    ('D7', 'nutrition', 1, 2.705), ('D7', 'concentration', 1, 2.299), ('D7', 'concentration', 1, 2.299), ('D7', 'student', 1, 1.452),
    ('D8', 'institutional_support', 1, 2.705), ('D8', 'counseling', 1, 2.299), ('D8', 'counseling', 1, 2.299), ('D8', 'student', 1, 1.452),
    ('D9', 'social_media', 1, 2.705), ('D9', 'social_media', 1, 2.705), ('D9', 'anxiety', 1, 2.299), ('D9', 'student', 1, 1.452), ('D9', 'anxiety', 1, 2.299),
    ('D10', 'self_regulation', 1, 2.705), ('D10', 'emotional', 1, 2.299)
) AS x(document_code, normalized_term, raw_frequency, weighted_frequency)
JOIN documents d ON d.code = x.document_code
JOIN terms t ON t.normalized_term = x.normalized_term
GROUP BY d.document_id, t.term_id
ON CONFLICT (document_id, term_id)
DO UPDATE SET
    raw_frequency = EXCLUDED.raw_frequency,
    weighted_frequency = EXCLUDED.weighted_frequency;

-- Insert one demo LSI model and simple two-dimensional document vectors.
INSERT INTO latent_models (k_value, source_matrix_type, notes)
VALUES (2, 'weighted_frequency', 'Demo LSI model with k=2 for persistence verification');

INSERT INTO latent_document_vectors (latent_model_id, document_id, component_index, component_value)
SELECT lm.latent_model_id, d.document_id, x.component_index, x.component_value
FROM latent_models lm
CROSS JOIN (
    VALUES
    ('D1', 0, 0.92), ('D1', 1, 0.18),
    ('D2', 0, 0.86), ('D2', 1, 0.30),
    ('D3', 0, 0.72), ('D3', 1, 0.44),
    ('D4', 0, 0.20), ('D4', 1, 0.90),
    ('D5', 0, 0.24), ('D5', 1, 0.86),
    ('D6', 0, 0.30), ('D6', 1, 0.72),
    ('D7', 0, 0.58), ('D7', 1, 0.45),
    ('D8', 0, 0.50), ('D8', 1, 0.66),
    ('D9', 0, 0.82), ('D9', 1, 0.40),
    ('D10', 0, 0.28), ('D10', 1, 0.70)
) AS x(document_code, component_index, component_value)
JOIN documents d ON d.code = x.document_code
WHERE lm.notes = 'Demo LSI model with k=2 for persistence verification';

-- Insert one persisted query run and ranked results as an initial evidence row.
INSERT INTO query_runs (query_text, similarity_metric, n_value)
VALUES ('academic stress anxiety', 'cosine', 5);

INSERT INTO query_results (query_run_id, rank_position, document_id, score)
SELECT qr.query_run_id, x.rank_position, d.document_id, x.score
FROM query_runs qr
JOIN (
    VALUES
    ('D1', 1, 0.95),
    ('D2', 2, 0.88),
    ('D9', 3, 0.84),
    ('D3', 4, 0.72),
    ('D7', 5, 0.51)
) AS x(document_code, rank_position, score) ON TRUE
JOIN documents d ON d.code = x.document_code
WHERE qr.query_text = 'academic stress anxiety'
  AND qr.similarity_metric = 'cosine';

COMMIT;
