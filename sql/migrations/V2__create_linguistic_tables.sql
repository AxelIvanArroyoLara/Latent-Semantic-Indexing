CREATE TABLE IF NOT EXISTS stop_words (
    stop_word_id BIGSERIAL PRIMARY KEY,
    language_code VARCHAR(10) NOT NULL DEFAULT 'en',
    word VARCHAR(100) NOT NULL,
    UNIQUE (language_code, word)
    );

CREATE TABLE IF NOT EXISTS suffix_rules (
    suffix_rule_id BIGSERIAL PRIMARY KEY,
    suffix VARCHAR(50) NOT NULL,
    replacement VARCHAR(50) NOT NULL DEFAULT '',
    priority INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (suffix, replacement)
);

CREATE TABLE IF NOT EXISTS term_synonyms (
    term_synonym_id BIGSERIAL PRIMARY KEY,
    canonical_term VARCHAR(150) NOT NULL,
    synonym_term VARCHAR(150) NOT NULL,
    direction_type VARCHAR(20) NOT NULL DEFAULT 'one_way',
    CHECK (direction_type IN ('one_way', 'bidirectional')),
    UNIQUE (canonical_term, synonym_term)
);

CREATE TABLE IF NOT EXISTS polysemy_rules (
    polysemy_rule_id BIGSERIAL PRIMARY KEY,
    ambiguous_term VARCHAR(150) NOT NULL,
    context_token VARCHAR(150) NOT NULL,
    assigned_sense VARCHAR(150) NOT NULL,
    priority INTEGER NOT NULL DEFAULT 0,
    UNIQUE (ambiguous_term, context_token, assigned_sense)
);
