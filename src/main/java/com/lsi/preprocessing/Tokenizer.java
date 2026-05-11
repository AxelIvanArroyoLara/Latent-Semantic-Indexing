package com.lsi.preprocessing;

import java.util.Arrays;
import java.util.List;

/**
 * Splits normalized text into individual tokens.
 */
public class Tokenizer {

    public List<String> tokenize(String normalizedText) {
        if (normalizedText == null || normalizedText.isBlank()) {
            return List.of();
        }

        return Arrays.stream(normalizedText.split("\\s+"))
                .filter(token -> !token.isBlank())
                .toList();
    }
}

