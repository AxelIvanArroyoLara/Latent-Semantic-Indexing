INSERT INTO suffix_rules (suffix, replacement, priority, enabled) VALUES
('ing', '', 100, TRUE),
('ed', '', 90, TRUE),
('ies', 'y', 80, TRUE),
('es', '', 70, TRUE),
('s', '', 60, TRUE),
('ly', '', 50, TRUE),
('ment', '', 40, TRUE),
('tion', '', 30, TRUE),
('ness', '', 20, TRUE);