package com.lsi.semantic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Maps equivalent domain terms to a canonical representation.
 */
public class SynonymExpander {

    private final Map<String, String> synonyms;

    public SynonymExpander() {
        this(defaultSynonyms());
    }

    public SynonymExpander(Map<String, String> synonyms) {
        this.synonyms = new LinkedHashMap<>(synonyms);
    }

    public List<String> normalizeAll(List<String> terms) {
        if (terms == null || terms.isEmpty()) {
            return List.of();
        }

        return terms.stream()
                .map(this::normalize)
                .filter(term -> !term.isBlank())
                .toList();
    }

    public String normalize(String term) {
        if (term == null || term.isBlank()) {
            return "";
        }

        String normalized = term.trim().toLowerCase(Locale.ROOT);
        return synonyms.getOrDefault(normalized, normalized);
    }

    private static Map<String, String> defaultSynonyms() {
        Map<String, String> values = new LinkedHashMap<>();

        values.put("worry", "anxiety");
        values.put("nervousness", "anxiety");
        values.put("pressure", "stress");
        values.put("well-being", "wellbeing");
        values.put("counselling", "counseling");
        values.put("therapy", "counseling");
        values.put("exercise", "physical_activity");
        values.put("focus", "concentration");
        values.put("rest", "sleep");
        values.put("sadness", "depression");
        values.put("network", "social_media");

        return values;
    }
}

