package com.lsi.model;

public record QueryRequest(
        String text,
        String metric,
        int topN
) {
    public QueryRequest {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text cannot be blank");
        }

        if (metric == null || metric.isBlank()) {
            metric = "cosine";
        }

        if (topN <= 0) {
            throw new IllegalArgumentException("topN must be positive");
        }
    }
}
