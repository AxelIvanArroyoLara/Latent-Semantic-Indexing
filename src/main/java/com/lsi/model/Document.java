package com.lsi.model;

/**
 * In-memory document used by the local indexing pipeline.
 */
public record Document(
        String code,
        String title,
        String source,
        String rawText,
        String normalizedText
) {

    public Document {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Document code cannot be blank");
        }

        if (title == null || title.isBlank()) {
            title = code;
        }

        if (source == null) {
            source = "";
        }

        if (rawText == null) {
            rawText = "";
        }

        if (normalizedText == null) {
            normalizedText = "";
        }
    }

    public String textForProcessing() {
        if (!normalizedText.isBlank()) {
            return normalizedText;
        }

        return rawText;
    }
}

