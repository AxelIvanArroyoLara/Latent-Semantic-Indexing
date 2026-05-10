package com.equipo.documentbase.domain;

import java.util.List;
import java.util.Map;

public record SemanticDocument(
        String code,
        List<String> canonicalTerms,
        Map<String, Object> metadata
) {}
