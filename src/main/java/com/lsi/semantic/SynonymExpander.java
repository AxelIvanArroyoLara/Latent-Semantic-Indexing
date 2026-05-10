package com.lsi.semantic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Handles controlled synonym normalization.
 *
 * This class does not use database access.
 * It reads a small CSV dictionary and replaces known synonyms
 * with their canonical term.
 */
public class SynonymExpander {

    private static final String DEFAULT_RESOURCE = "/synonyms.csv";

    private final Map<String, String> synonymToCanonical;

    public SynonymExpander() {
        this(loadDefaultDictionary());
    }

    public SynonymExpander(Map<String, String> synonymToCanonical) {
        Objects.requireNonNull(synonymToCanonical, "synonymToCanonical cannot be null");

        this.synonymToCanonical = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : synonymToCanonical.entrySet()) {
            String synonym = normalize(entry.getKey());
            String canonical = normalize(entry.getValue());

            if (!synonym.isBlank() && !canonical.isBlank()) {
                this.synonymToCanonical.put(synonym, canonical);
            }
        }
    }

    public List<String> expand(List<String> tokens) {
        Objects.requireNonNull(tokens, "tokens cannot be null");

        List<String> result = new ArrayList<>();

        for (String token : tokens) {
            String canonical = canonicalize(token);

            if (!canonical.isBlank()) {
                result.add(canonical);
            }
        }

        return result;
    }

    public String canonicalize(String token) {
        String normalized = normalize(token);

        if (normalized.isBlank()) {
            return normalized;
        }

        return synonymToCanonical.getOrDefault(normalized, normalized);
    }

    public Map<String, String> dictionary() {
        return Collections.unmodifiableMap(synonymToCanonical);
    }

    private static Map<String, String> loadDefaultDictionary() {
        Map<String, String> dictionary = new LinkedHashMap<>();

        try (InputStream input = SynonymExpander.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (input == null) {
                return dictionary;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.isBlank() || line.startsWith("#")) {
                        continue;
                    }

                    if (line.equalsIgnoreCase("synonym,canonical")) {
                        continue;
                    }

                    String[] parts = line.split(",", -1);

                    if (parts.length < 2) {
                        continue;
                    }

                    String synonym = normalize(parts[0]);
                    String canonical = normalize(parts[1]);

                    if (!synonym.isBlank() && !canonical.isBlank()) {
                        dictionary.put(synonym, canonical);
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load synonyms.csv", e);
        }

        return dictionary;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "_");
    }
}
