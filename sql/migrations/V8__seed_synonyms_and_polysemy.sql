INSERT INTO term_synonyms (canonical_term, synonym_term, direction_type) VALUES
('stress', 'tension', 'bidirectional'),
('stress', 'pressure', 'bidirectional'),
('anxiety', 'worry', 'bidirectional'),
('anxiety', 'nervousness', 'bidirectional'),
('burnout', 'exhaustion', 'bidirectional'),
('depression', 'sadness', 'bidirectional'),
('sleep', 'rest', 'bidirectional'),
('exercise', 'physical_activity', 'bidirectional'),
('support', 'assistance', 'bidirectional'),
('counseling', 'therapy', 'bidirectional'),
('wellbeing', 'wellness', 'bidirectional');

INSERT INTO polysemy_rules (ambiguous_term, context_token, assigned_sense, priority) VALUES
('support', 'university', 'institutional_support', 100),
('support', 'campus', 'institutional_support', 90),
('support', 'counseling', 'psychological_support', 80),
('support', 'emotional', 'emotional_support', 70),
('pressure', 'academic', 'academic_pressure', 100),
('pressure', 'social', 'social_pressure', 90),
('performance', 'school', 'academic_performance', 100),
('performance', 'academic', 'academic_performance', 90),
('network', 'social', 'social_media_network', 100);