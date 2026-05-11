-- Summarizes linguistic resources used by the preprocessing and semantic modules.

SELECT 'stop_words' AS resource_name, COUNT(*) AS total_rows
FROM stop_words

UNION ALL

SELECT 'suffix_rules' AS resource_name, COUNT(*) AS total_rows
FROM suffix_rules

UNION ALL

SELECT 'term_synonyms' AS resource_name, COUNT(*) AS total_rows
FROM term_synonyms

UNION ALL

SELECT 'polysemy_rules' AS resource_name, COUNT(*) AS total_rows
FROM polysemy_rules;