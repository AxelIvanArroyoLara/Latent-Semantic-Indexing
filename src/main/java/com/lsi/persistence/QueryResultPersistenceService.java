package com.lsi.persistence;

import com.lsi.query.QueryProcessor;
import com.lsi.query.RankingService;

import java.util.Optional;

/**
 * Persists query executions produced by the query module.
 *
 * The query module calculates rankings; this service only connects those
 * finished results with the relational query log tables.
 */
public class QueryResultPersistenceService {

    private final QueryLogRepository queryLogRepository;
    private final DocumentRepository documentRepository;

    public QueryResultPersistenceService() {
        this(new QueryLogRepository(), new DocumentRepository());
    }

    public QueryResultPersistenceService(
            QueryLogRepository queryLogRepository,
            DocumentRepository documentRepository
    ) {
        this.queryLogRepository = queryLogRepository;
        this.documentRepository = documentRepository;
    }

    public PersistenceSummary persist(QueryProcessor.QueryRun run) {
        if (run == null) {
            throw new IllegalArgumentException("Query run cannot be null");
        }

        long queryRunId = queryLogRepository.saveQueryRun(
                run.queryText(),
                run.metric(),
                run.topN()
        );

        int persistedResults = 0;
        int position = 1;

        for (RankingService.RankedDocument result : run.results()) {
            Optional<DocumentRepository.DocumentRow> document =
                    documentRepository.findByCode(result.code());

            if (document.isEmpty()) {
                position++;
                continue;
            }

            queryLogRepository.saveQueryResult(
                    queryRunId,
                    position,
                    document.get().documentId(),
                    result.score()
            );

            persistedResults++;
            position++;
        }

        return new PersistenceSummary(queryRunId, persistedResults);
    }

    public record PersistenceSummary(
            long queryRunId,
            int persistedResults
    ) {
    }
}
