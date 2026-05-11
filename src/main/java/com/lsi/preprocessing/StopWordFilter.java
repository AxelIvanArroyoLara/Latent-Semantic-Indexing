package com.lsi.preprocessing;

import java.util.List;
import java.util.Set;

/**
 * Removes common English stop words from a token list.
 */
public class StopWordFilter {

    private final Set<String> stopWords;

    public StopWordFilter() {
        this.stopWords = Set.of(
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
}

