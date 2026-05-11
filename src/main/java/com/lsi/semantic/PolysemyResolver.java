package com.lsi.semantic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Resolves controlled polysemy using adjacent token rules.
 *
 * Example:
 * academic pressure -> academic_pressure
 * emotional support -> emotional_support
 * university support -> institutional_support
 */
public class PolysemyResolver {

    private static final String DEFAULT_RESOURCE = "polysemy_rules.csv";

    private final Map<String, String> rulesByPair;

    public PolysemyResolver() {
        this.rulesByPair = loadRulesFromResource(DEFAULT_RESOURCE);
    }

    public PolysemyResolver(Map<String, String> rulesByPair) {
        this.rulesByPair = new LinkedHashMap<>();

        if (rulesByPair != null) {
            for (Map.Entry<String, String> entry : rulesByPair.entrySet()) {
                addRuleFromFlexibleKey(entry.getKey(), entry.getValue());
            }
        }
    }

    public static PolysemyResolver fromResource(String resourceName) {
        PolysemyResolver resolver = new PolysemyResolver(new LinkedHashMap<>());
        resolver.rulesByPair.putAll(loadRulesFromResource(resourceName));
        return resolver;
    }

    public static PolysemyResolver fromCsv(Path path) {
        Objects.requireNonNull(path, "path cannot be null");

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            PolysemyResolver resolver = new PolysemyResolver(new LinkedHashMap<>());
            resolver.rulesByPair.putAll(readRules(reader));
            return resolver;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load polysemy rules from file: " + path, exception);
        }
    }

    public List<String> resolve(List<String> tokens) {
        Objects.requireNonNull(tokens, "tokens cannot be null");

        List<String> resolved = new ArrayList<>();

        int index = 0;
        while (index < tokens.size()) {
            String current = normalizeToken(tokens.get(index));

            if (current.isBlank()) {
                index++;
                continue;
            }

            if (index + 1 < tokens.size()) {
                String next = normalizeToken(tokens.get(index + 1));
                String replacement = findReplacement(current, next);

                if (replacement != null) {
                    resolved.add(replacement);
                    index += 2;
                    continue;
                }
            }

            resolved.add(current);
            index++;
        }

        return resolved;
    }

    public List<String> resolvePolysemy(List<String> tokens) {
        return resolve(tokens);
    }

    public List<String> normalize(List<String> tokens) {
        return resolve(tokens);
    }

    public List<String> process(List<String> tokens) {
        return resolve(tokens);
    }

    private String findReplacement(String left, String right) {
        return rulesByPair.get(buildKey(left, right));
    }

    private void addRuleFromFlexibleKey(String key, String value) {
        if (key == null || value == null) {
            return;
        }

        String cleanKey = key.trim().toLowerCase();
        String cleanValue = normalizeToken(value);

        if (cleanKey.isBlank() || cleanValue.isBlank()) {
            return;
        }

        String[] parts;

        if (cleanKey.contains("|")) {
            parts = cleanKey.split("\\|");
        } else if (cleanKey.contains(",")) {
            parts = cleanKey.split(",");
        } else {
            parts = cleanKey.split("\\s+");
        }

        if (parts.length < 2) {
            return;
        }

        addRule(parts[0], parts[1], cleanValue);
    }

    private static Map<String, String> loadRulesFromResource(String resourceName) {
        String cleanName = resourceName.startsWith("/") ? resourceName.substring(1) : resourceName;

        try (InputStream inputStream = PolysemyResolver.class.getClassLoader().getResourceAsStream(cleanName)) {
            if (inputStream == null) {
                return new LinkedHashMap<>();
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return readRules(reader);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load polysemy rules from resource: " + resourceName, exception);
        }
    }

    private static Map<String, String> readRules(BufferedReader reader) throws IOException {
        Map<String, String> rules = new LinkedHashMap<>();
        String line;

        while ((line = reader.readLine()) != null) {
            String cleanLine = line.trim();

            if (cleanLine.isBlank() || cleanLine.startsWith("#")) {
                continue;
            }

            String[] parts = cleanLine.split(",");

            if (parts.length < 3) {
                continue;
            }

            String firstToken = normalizeToken(parts[0]);
            String secondToken = normalizeToken(parts[1]);
            String resolvedTerm = normalizeToken(parts[2]);

            if (firstToken.isBlank() || secondToken.isBlank() || resolvedTerm.isBlank()) {
                continue;
            }

            rules.put(buildKey(firstToken, secondToken), resolvedTerm);
            rules.put(buildKey(secondToken, firstToken), resolvedTerm);
        }

        return rules;
    }

    private static void addRuleToMap(Map<String, String> map, String left, String right, String resolved) {
        map.put(buildKey(left, right), resolved);
        map.put(buildKey(right, left), resolved);
    }

    private void addRule(String left, String right, String resolved) {
        addRuleToMap(this.rulesByPair, left, right, resolved);
    }

    private static String buildKey(String left, String right) {
        return normalizeToken(left) + "|" + normalizeToken(right);
    }

    private static String normalizeToken(String token) {
        if (token == null) {
            return "";
        }

        return token.trim()
                .toLowerCase()
                .replace("-", "_")
                .replace(" ", "_");
    }
}
