package com.lsi.query;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankingServiceTest {

    @Test
    void shouldCalculateCosineSimilarity() {
        SimilarityCalculator calculator = new SimilarityCalculator();

        double result = calculator.cosine(
                new double[]{1.0, 0.0},
                new double[]{1.0, 0.0}
        );

        assertEquals(1.0, result, 0.0001);
    }

    @Test
    void shouldCalculateJaccardSimilarity() {
        SimilarityCalculator calculator = new SimilarityCalculator();

        double result = calculator.jaccard(
                Set.of("academic", "stress", "student"),
                Set.of("stress", "student", "sleep")
        );

        assertEquals(0.5, result, 0.0001);
    }

    @Test
    void shouldCalculateEuclideanDistance() {
        DissimilarityCalculator calculator = new DissimilarityCalculator();

        double result = calculator.euclidean(
                new double[]{0.0, 0.0},
                new double[]{3.0, 4.0}
        );

        assertEquals(5.0, result, 0.0001);
    }

    @Test
    void shouldRankByCosineDescending() {
        RankingService service = new RankingService();

        List<RankingService.DocumentVector> documents = List.of(
                new RankingService.DocumentVector("D1", "Close", new double[]{1.0, 0.0}),
                new RankingService.DocumentVector("D2", "Far", new double[]{0.0, 1.0})
        );

        List<RankingService.RankedDocument> result = service.rankByVector(
                new double[]{1.0, 0.0},
                documents,
                "cosine",
                2
        );

        assertEquals("D1", result.get(0).code());
    }

    @Test
    void shouldRankByEuclideanAscending() {
        RankingService service = new RankingService();

        List<RankingService.DocumentVector> documents = List.of(
                new RankingService.DocumentVector("D1", "Close", new double[]{1.0, 0.0}),
                new RankingService.DocumentVector("D2", "Far", new double[]{9.0, 9.0})
        );

        List<RankingService.RankedDocument> result = service.rankByVector(
                new double[]{1.0, 0.0},
                documents,
                "euclidean",
                2
        );

        assertEquals("D1", result.get(0).code());
    }

    @Test
    void shouldProcessQueryWithFixtureData() {
        QueryProcessor processor = new QueryProcessor();

        QueryProcessor.QueryRun result = processor.query(
                "academic stress anxiety",
                3,
                "cosine"
        );

        assertEquals(3, result.results().size());
        assertFalse(result.results().isEmpty());
        assertTrue(result.results().get(0).score() > 0.0);
    }

    @Test
    void shouldCompareDocumentsWithFixtureData() {
        QueryProcessor processor = new QueryProcessor();

        DocumentSimilarityService.ComparisonResult result = processor.compareDocuments("D1", "D3");

        assertEquals("D1", result.firstCode());
        assertEquals("D3", result.secondCode());
        assertTrue(result.cosine() > 0.0);
        assertTrue(result.jaccard() > 0.0);
        assertTrue(result.euclidean() >= 0.0);
    }
}
