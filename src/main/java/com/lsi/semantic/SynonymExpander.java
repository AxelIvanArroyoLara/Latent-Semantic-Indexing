package com.lsi.semantic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
        this(loadSynonyms());
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

    private static Map<String, String> loadSynonyms() {
        InputStream stream = SynonymExpander.class.getResourceAsStream("/synonyms.csv");

        if (stream == null) {
            return defaultSynonyms();
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        )) {
            Map<String, String> values = new LinkedHashMap<>();
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();

                if (trimmed.isBlank() || trimmed.startsWith("#")) {
                    continue;
                }

                if (firstLine) {
                    firstLine = false;
                    if (trimmed.toLowerCase(Locale.ROOT).startsWith("canonical_term,")) {
                        continue;
                    }
                }

                String[] parts = trimmed.split(",", 3);

                if (parts.length < 2) {
                    continue;
                }

                String canonical = parts[0].trim().toLowerCase(Locale.ROOT);
                String synonym = parts[1].trim().toLowerCase(Locale.ROOT);

                if (!canonical.isBlank() && !synonym.isBlank()) {
                    values.put(synonym, canonical);
                }
            }

            return values.isEmpty() ? defaultSynonyms() : values;
        } catch (IOException e) {
            return defaultSynonyms();
        }
    }
}

