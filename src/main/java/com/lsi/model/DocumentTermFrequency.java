package com.lsi.model;

public record DocumentTermFrequency(
        String documentCode,
        String term,
        double rawFrequency,
        double weightedFrequency
) {
    public DocumentTermFrequency {
        if (documentCode == null || documentCode.isBlank()) {
            throw new IllegalArgumentException("documentCode cannot be blank");
        }

        if (term == null || term.isBlank()) {
            throw new IllegalArgumentException("term cannot be blank");
        }

        if (rawFrequency < 0 || weightedFrequency < 0) {
            throw new IllegalArgumentException("frequencies cannot be negative");
        }
    }
}
