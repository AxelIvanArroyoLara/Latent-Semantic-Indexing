package com.lsi.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class QueryLogRepositoryTest {

    private DocumentRepository documentRepository;
    private QueryLogRepository queryLogRepository;

    @BeforeEach
    void setUp() {
        DatabaseTestSupport.assumeDatabaseAvailable();
        documentRepository = new DocumentRepository();
        queryLogRepository = new QueryLogRepository();

        queryLogRepository.deleteAll();
        documentRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindQueryRunById() {
        long queryRunId = queryLogRepository.saveQueryRun(
                "academic stress anxiety",
                "cosine",
                5
        );

        Optional<QueryLogRepository.QueryRunRow> result =
                queryLogRepository.findQueryRunById(queryRunId);

        assertTrue(result.isPresent(), "Query run should exist");
        assertEquals(queryRunId, result.get().queryRunId());
        assertEquals("academic stress anxiety", result.get().queryText());
        assertEquals("cosine", result.get().similarityMetric());
        assertEquals(5, result.get().nValue());
        assertNotNull(result.get().createdAt());
    }

    @Test
    void shouldListAllQueryRuns() {
        queryLogRepository.saveQueryRun(
                "academic stress anxiety",
                "cosine",
                5
        );

        queryLogRepository.saveQueryRun(
                "sleep wellbeing",
                "jaccard",
                3
        );

        List<QueryLogRepository.QueryRunRow> queryRuns =
                queryLogRepository.findAllQueryRuns();

        assertEquals(2, queryRuns.size());

        assertEquals("academic stress anxiety", queryRuns.get(0).queryText());
        assertEquals("cosine", queryRuns.get(0).similarityMetric());

        assertEquals("sleep wellbeing", queryRuns.get(1).queryText());
        assertEquals("jaccard", queryRuns.get(1).similarityMetric());
    }

    @Test
    void shouldSaveAndFindQueryResultsByQueryRunId() {
        long documentOneId = documentRepository.save(
                "D1",
                "Academic Stress in University Students",
                "data/raw/D1.txt",
                "Academic stress affects university students.",
                "academic stress affects university students",
                "en"
        );

        long documentTwoId = documentRepository.save(
                "D2",
                "Anxiety and School Performance",
                "data/raw/D2.txt",
                "Anxiety can affect school performance.",
                "anxiety can affect school performance",
                "en"
        );

        long queryRunId = queryLogRepository.saveQueryRun(
                "academic stress anxiety",
                "cosine",
                2
        );

        queryLogRepository.saveQueryResult(queryRunId, 1, documentOneId, 0.95);
        queryLogRepository.saveQueryResult(queryRunId, 2, documentTwoId, 0.82);

        List<QueryLogRepository.QueryResultRow> results =
                queryLogRepository.findResultsByQueryRunId(queryRunId);

        assertEquals(2, results.size());

        assertEquals(queryRunId, results.get(0).queryRunId());
        assertEquals(1, results.get(0).rankPosition());
        assertEquals(documentOneId, results.get(0).documentId());
        assertEquals(0.95, results.get(0).score());

        assertEquals(queryRunId, results.get(1).queryRunId());
        assertEquals(2, results.get(1).rankPosition());
        assertEquals(documentTwoId, results.get(1).documentId());
        assertEquals(0.82, results.get(1).score());
    }

    @Test
    void shouldUpdateQueryResultWhenRankAlreadyExists() {
        long documentOneId = documentRepository.save(
                "D1",
                "Academic Stress in University Students",
                "data/raw/D1.txt",
                "Academic stress affects university students.",
                "academic stress affects university students",
                "en"
        );

        long documentTwoId = documentRepository.save(
                "D2",
                "Anxiety and School Performance",
                "data/raw/D2.txt",
                "Anxiety can affect school performance.",
                "anxiety can affect school performance",
                "en"
        );

        long queryRunId = queryLogRepository.saveQueryRun(
                "academic stress anxiety",
                "cosine",
                1
        );

        queryLogRepository.saveQueryResult(queryRunId, 1, documentOneId, 0.70);
        queryLogRepository.saveQueryResult(queryRunId, 1, documentTwoId, 0.91);

        List<QueryLogRepository.QueryResultRow> results =
                queryLogRepository.findResultsByQueryRunId(queryRunId);

        assertEquals(1, results.size());
        assertEquals(1, results.get(0).rankPosition());
        assertEquals(documentTwoId, results.get(0).documentId());
        assertEquals(0.91, results.get(0).score());
    }

    @Test
    void shouldListAllQueryResults() {
        long documentId = documentRepository.save(
                "D1",
                "Academic Stress in University Students",
                "data/raw/D1.txt",
                "Academic stress affects university students.",
                "academic stress affects university students",
                "en"
        );

        long queryRunId = queryLogRepository.saveQueryRun(
                "academic stress",
                "cosine",
                1
        );

        queryLogRepository.saveQueryResult(queryRunId, 1, documentId, 0.88);

        List<QueryLogRepository.QueryResultRow> results =
                queryLogRepository.findAllQueryResults();

        assertEquals(1, results.size());
        assertEquals(queryRunId, results.get(0).queryRunId());
        assertEquals(1, results.get(0).rankPosition());
        assertEquals(documentId, results.get(0).documentId());
        assertEquals(0.88, results.get(0).score());
    }

    @Test
    void shouldReturnEmptyWhenQueryRunDoesNotExist() {
        Optional<QueryLogRepository.QueryRunRow> result =
                queryLogRepository.findQueryRunById(999L);

        assertTrue(result.isEmpty(), "Query run should not exist");
    }

    @Test
    void shouldReturnEmptyResultsWhenQueryRunHasNoResults() {
        long queryRunId = queryLogRepository.saveQueryRun(
                "sleep wellbeing",
                "jaccard",
                5
        );

        List<QueryLogRepository.QueryResultRow> results =
                queryLogRepository.findResultsByQueryRunId(queryRunId);

        assertTrue(results.isEmpty(), "Query run should not have results");
    }
}
