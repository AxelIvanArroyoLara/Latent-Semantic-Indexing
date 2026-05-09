package com.lsi.preprocessing;

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
        this.suffixRules = defaultSuffixRules();
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
}
