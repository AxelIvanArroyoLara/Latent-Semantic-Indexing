package com.lsi.preprocessing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies simple English suffix rules to reduce tokens to stems.
 * This is a lightweight rule-based stemmer for the LSI preprocessing stage.
 */
public class Stemmer {

    private final Map<String, String> suffixRules;

    public Stemmer() {
        this.suffixRules = loadSuffixRules();
    }

    public Stemmer(Map<String, String> suffixRules) {
        this.suffixRules = suffixRules;
    }

    public String stem(String token) {
        if (token == null || token.isBlank()) {
            return "";
        }

        if (token.endsWith("ss")) {
            return token;
        }

        for (Map.Entry<String, String> rule : suffixRules.entrySet()) {
            String suffix = rule.getKey();
            String replacement = rule.getValue();

            if (token.endsWith(suffix) && token.length() > suffix.length() + 2) {
                return token.substring(0, token.length() - suffix.length()) + replacement;
            }
        }

        return token;
    }

    public List<String> stemAll(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return List.of();
        }

        return tokens.stream()
                .map(this::stem)
                .toList();
    }

    private Map<String, String> defaultSuffixRules() {
        Map<String, String> rules = new LinkedHashMap<>();

        rules.put("ization", "ize");
        rules.put("ational", "ate");
        rules.put("fulness", "ful");
        rules.put("ousness", "ous");
        rules.put("iveness", "ive");
        rules.put("tional", "tion");
        rules.put("biliti", "ble");
        rules.put("ing", "");
        rules.put("edly", "");
        rules.put("ed", "");
        rules.put("ies", "y");
        rules.put("es", "");
        rules.put("s", "");

        return rules;
    }

    private Map<String, String> loadSuffixRules() {
        InputStream stream = Stemmer.class.getResourceAsStream("/suffixes.txt");

        if (stream == null) {
            return defaultSuffixRules();
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        )) {
            Map<String, String> rules = new LinkedHashMap<>();
            String line;

            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();

                if (trimmed.isBlank() || trimmed.startsWith("#")) {
                    continue;
                }

                String[] parts = trimmed.split("=", 2);
                String suffix = parts[0].trim();
                String replacement = parts.length > 1 ? parts[1].trim() : "";

                if (!suffix.isBlank()) {
                    rules.put(suffix, replacement);
                }
            }

            return rules.isEmpty() ? defaultSuffixRules() : rules;
        } catch (IOException e) {
            return defaultSuffixRules();
        }
    }
}

