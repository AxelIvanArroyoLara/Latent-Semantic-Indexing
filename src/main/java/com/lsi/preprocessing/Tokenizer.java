package com.lsi.preprocessing;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Splits normalized text into individual tokens.
 */
public class Tokenizer {

    private static final Set<String> PDF_EXTRACTION_NOISE = Set.of(
            "http",
            "https",
            "www",
            "doi",
            "org",
            "com",
            "edu",
            "isbn",
            "issn",
            "pdf",
            "fig",
            "figure",
            "table",
            "content",
            "copyright",
            "license",
            "creativecommons",
            "anuscript",
            "uthor",
            "achf",
            "bas",
            "cis",
            "ijw",
            "phda",
            "mph",
            "edd",
            "eprint",
            "whiterose"
    );

    public List<String> tokenize(String normalizedText) {
        if (normalizedText == null || normalizedText.isBlank()) {
            return List.of();
        }

        return Arrays.stream(normalizedText.split("\\s+"))
                .filter(token -> !token.isBlank())
                .filter(token -> token.length() >= 3)
                .filter(token -> !token.matches("\\d+"))
                .filter(token -> !token.matches(".*\\d.*"))
                .filter(token -> !PDF_EXTRACTION_NOISE.contains(token))
                .filter(this::hasVowel)
                .toList();
    }

    private boolean hasVowel(String token) {
        return token.matches(".*[aeiou].*");
    }
}

