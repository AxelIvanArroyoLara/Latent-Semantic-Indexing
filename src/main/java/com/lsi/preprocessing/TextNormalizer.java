package com.lsi.preprocessing;

import java.text.Normalizer;

/**
 * Normalizes raw text before tokenization.
 * It lowercases text, removes accents, removes punctuation,
 * and collapses repeated spaces.
 */
public class TextNormalizer {

    public String normalize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String normalized = text.toLowerCase();

        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");

        normalized = normalized.replaceAll("[^a-z0-9\\s]", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();

        return normalized;
    }
}

