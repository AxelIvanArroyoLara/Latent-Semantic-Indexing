-- Shows the linguistic and semantic resources configured in the database.

SELECT
    'stop_word' AS resource_type,
    word AS main_value,
    language_code AS secondary_value,
    NULL AS extra_value,
    NULL AS priority
FROM stop_words

UNION ALL

SELECT
    'suffix_rule' AS resource_type,
    suffix AS main_value,
    replacement AS secondary_value,
    CASE WHEN enabled THEN 'enabled' ELSE 'disabled' END AS extra_value,
    priority
FROM suffix_rules

UNION ALL

SELECT
    'term_synonym' AS resource_type,
    canonical_term AS main_value,
    synonym_term AS secondary_value,
    direction_type AS extra_value,
    NULL AS priority
FROM term_synonyms

UNION ALL

SELECT
    'polysemy_rule' AS resource_type,
    ambiguous_term AS main_value,
    context_token AS secondary_value,
    assigned_sense AS extra_value,
    priority
FROM polysemy_rules

ORDER BY
    resource_type,
    priority DESC NULLS LAST,
    main_value;