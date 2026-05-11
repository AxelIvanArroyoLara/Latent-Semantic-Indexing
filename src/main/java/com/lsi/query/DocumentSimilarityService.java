package com.lsi.query;

import java.util.Set;

public class DocumentSimilarityService {

    private final SimilarityCalculator similarityCalculator;
    private final DissimilarityCalculator dissimilarityCalculator;

    public DocumentSimilarityService() {
        this(new SimilarityCalculator(), new DissimilarityCalculator());
    }

    public DocumentSimilarityService(
            SimilarityCalculator similarityCalculator,
            DissimilarityCalculator dissimilarityCalculator
    ) {
        this.similarityCalculator = similarityCalculator;
        this.dissimilarityCalculator = dissimilarityCalculator;
    }

    public ComparisonResult compare(DocumentProfile first, DocumentProfile second) {
        double cosine = similarityCalculator.cosine(first.vector(), second.vector());
        double jaccard = similarityCalculator.jaccard(first.terms(), second.terms());
        double euclidean = dissimilarityCalculator.euclidean(first.vector(), second.vector());

        return new ComparisonResult(
                first.code(),
                second.code(),
                first.title(),
                second.title(),
                cosine,
                jaccard,
                euclidean
        );
    }

    public record DocumentProfile(
            String code,
            String title,
            double[] vector,
            Set<String> terms
    ) {
    }

    public record ComparisonResult(
            String firstCode,
            String secondCode,
            String firstTitle,
            String secondTitle,
            double cosine,
            double jaccard,
            double euclidean
    ) {
    }
}

