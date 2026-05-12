package com.lsi.model;

public record QueryResult(
        String documentCode,
        String title,
        int rankPosition,
        double score
) {
    public QueryResult {
        if (documentCode == null || documentCode.isBlank()) {
            throw new IllegalArgumentException("documentCode cannot be blank");
        }

        if (title == null) {
            title = "";
        }

        if (rankPosition <= 0) {
            throw new IllegalArgumentException("rankPosition must be positive");
        }
    }
}
