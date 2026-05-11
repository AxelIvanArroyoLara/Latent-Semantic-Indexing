package com.lsi.model;

import java.util.List;
import java.util.Map;

public record SemanticDocument(
        String code,
        List<String> canonicalTerms,
        Map<String, Object> metadata
) {}

