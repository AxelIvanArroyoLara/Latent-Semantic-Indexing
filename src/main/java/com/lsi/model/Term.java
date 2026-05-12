package com.lsi.model;

public record Term(
        String normalizedTerm,
        String canonicalTerm,
        String stem,
        String senseLabel
) {
    public Term {
        if (normalizedTerm == null || normalizedTerm.isBlank()) {
            throw new IllegalArgumentException("normalizedTerm cannot be blank");
        }

        if (canonicalTerm == null || canonicalTerm.isBlank()) {
            throw new IllegalArgumentException("canonicalTerm cannot be blank");
        }

        if (stem == null) {
            stem = "";
        }
    }
}
