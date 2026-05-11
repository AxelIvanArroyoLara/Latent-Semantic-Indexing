package com.lsi.semantic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Applies deterministic context rules for frequent domain phrases.
 */
public class PolysemyResolver {

    private final Map<String, String> pairRules;

    public PolysemyResolver() {
        this(defaultPairRules());
    }

    public PolysemyResolver(Map<String, String> pairRules) {
        this.pairRules = new LinkedHashMap<>(pairRules);
    }

    public List<String> resolve(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return List.of();
        }

        List<String> resolved = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            String current = normalize(tokens.get(i));

            if (current.isBlank()) {
                continue;
            }

            if (i + 1 < tokens.size()) {
                String next = normalize(tokens.get(i + 1));
                String canonical = pairRules.get(key(current, next));

                if (canonical != null) {
                    resolved.add(canonical);
                    i++;
                    continue;
                }
            }

            resolved.add(current);
        }

        return resolved;
    }

    private static Map<String, String> defaultPairRules() {
        Map<String, String> values = new LinkedHashMap<>();

        addSymmetric(values, "support", "university", "institutional_support");
        addSymmetric(values, "support", "emotional", "emotional_support");
        addSymmetric(values, "health", "mental", "mental_health");
        addSymmetric(values, "stress", "academic", "academic_stress");
        addSymmetric(values, "pressure", "academic", "academic_pressure");
        addSymmetric(values, "media", "social", "social_media");
        addSymmetric(values, "sleep", "quality", "sleep_quality");
        addSymmetric(values, "campus", "wellbeing", "campus_wellbeing");
        addSymmetric(values, "counseling", "services", "counseling_services");
        addSymmetric(values, "help", "seeking", "help_seeking");
        addSymmetric(values, "self", "regulation", "self_regulation");
        addSymmetric(values, "physical", "activity", "physical_activity");

        return values;
    }

    private static void addSymmetric(
            Map<String, String> rules,
            String first,
            String second,
            String canonical
    ) {
        rules.put(key(first, second), canonical);
        rules.put(key(second, first), canonical);
    }

    private static String key(String first, String second) {
        return normalize(first) + "\u0000" + normalize(second);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }
}

