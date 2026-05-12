package com.lsi.preprocessing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Removes common English stop words from a token list.
 */
public class StopWordFilter {

    private final Set<String> stopWords;

    public StopWordFilter() {
        this.stopWords = loadStopWords();
    }

    public StopWordFilter(Set<String> stopWords) {
        this.stopWords = stopWords;
    }

    public List<String> filter(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return List.of();
        }

        return tokens.stream()
                .filter(token -> !stopWords.contains(token))
                .toList();
    }

    private Set<String> loadStopWords() {
        InputStream stream = StopWordFilter.class.getResourceAsStream("/stopwords.txt");

        if (stream == null) {
            return defaultStopWords();
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        )) {
            Set<String> values = new LinkedHashSet<>();
            String line;

            while ((line = reader.readLine()) != null) {
                String word = line.trim().toLowerCase();

                if (!word.isBlank() && !word.startsWith("#")) {
                    values.add(word);
                }
            }

            return values.isEmpty() ? defaultStopWords() : values;
        } catch (IOException e) {
            return defaultStopWords();
        }
    }

    private Set<String> defaultStopWords() {
        return Set.of(
                "a", "an", "the",
                "and", "or", "but",
                "of", "to", "in", "on", "at", "by", "for", "from",
                "with", "without", "about", "into", "over", "under",
                "is", "are", "was", "were", "be", "been", "being",
                "this", "that", "these", "those",
                "it", "its", "as", "not",
                "we", "they", "he", "she", "you", "i",
                "our", "their", "his", "her", "your", "my"
        );
    }
}

