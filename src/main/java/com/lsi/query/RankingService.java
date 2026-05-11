package com.lsi.query;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class RankingService {

    private final SimilarityCalculator similarityCalculator;
    private final DissimilarityCalculator dissimilarityCalculator;

    public RankingService() {
        this(new SimilarityCalculator(), new DissimilarityCalculator());
    }

    public RankingService(
            SimilarityCalculator similarityCalculator,
            DissimilarityCalculator dissimilarityCalculator
    ) {
        this.similarityCalculator = similarityCalculator;
        this.dissimilarityCalculator = dissimilarityCalculator;
    }

    public List<RankedDocument> rankByVector(
            double[] queryVector,
            List<DocumentVector> documents,
            String metric,
            int topN
    ) {
        List<RankedDocument> ranked = new ArrayList<>();

        for (DocumentVector document : documents) {
            double score;

            if ("euclidean".equalsIgnoreCase(metric)) {
                score = dissimilarityCalculator.euclidean(queryVector, document.vector());
            } else {
                score = similarityCalculator.cosine(queryVector, document.vector());
            }

            ranked.add(new RankedDocument(document.code(), document.title(), score));
        }

        sort(ranked, metric);
        return limit(ranked, topN);
    }

    public List<RankedDocument> rankByTerms(
            Set<String> queryTerms,
            List<DocumentTerms> documents,
            int topN
    ) {
        List<RankedDocument> ranked = new ArrayList<>();

        for (DocumentTerms document : documents) {
            double score = similarityCalculator.jaccard(queryTerms, document.terms());
            ranked.add(new RankedDocument(document.code(), document.title(), score));
        }

        sort(ranked, "jaccard");
        return limit(ranked, topN);
    }

    private void sort(List<RankedDocument> ranked, String metric) {
        Comparator<RankedDocument> comparator;

        if ("euclidean".equalsIgnoreCase(metric)) {
            comparator = Comparator
                    .comparingDouble(RankedDocument::score)
                    .thenComparing(RankedDocument::code);
        } else {
            comparator = Comparator
                    .comparingDouble(RankedDocument::score)
                    .reversed()
                    .thenComparing(RankedDocument::code);
        }

        ranked.sort(comparator);
    }

    private List<RankedDocument> limit(List<RankedDocument> ranked, int topN) {
        if (topN <= 0) {
            return List.of();
        }

        return ranked.stream()
                .limit(topN)
                .toList();
    }

    public record DocumentVector(
            String code,
            String title,
            double[] vector
    ) {
    }

    public record DocumentTerms(
            String code,
            String title,
            Set<String> terms
    ) {
    }

    public record RankedDocument(
            String code,
            String title,
            double score
    ) {
    }
}

