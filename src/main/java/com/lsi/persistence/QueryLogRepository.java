package com.lsi.persistence;

import com.lsi.config.DatabaseConfig;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for storing query executions and ranked query results.
 *
 * This class only persists query logs.
 * It does not calculate similarity, dissimilarity, rankings, or LSI projections.
 */
public class QueryLogRepository {

    private final DataSource dataSource;

    public QueryLogRepository() {
        this(DatabaseConfig.getDataSource());
    }

    public QueryLogRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long saveQueryRun(
            String queryText,
            String similarityMetric,
            int nValue
    ) {
        String sql = """
                INSERT INTO query_runs (query_text, similarity_metric, n_value)
                VALUES (?, ?, ?)
                RETURNING query_run_id
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, queryText);
            statement.setString(2, similarityMetric);
            statement.setInt(3, nValue);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("query_run_id");
                }
            }

            throw new SQLException("Query run insert did not return an id.");

        } catch (SQLException e) {
            throw new RepositoryException("Could not save query run.", e);
        }
    }

    public Optional<QueryRunRow> findQueryRunById(long queryRunId) {
        String sql = """
                SELECT query_run_id, query_text, similarity_metric, n_value, created_at
                FROM query_runs
                WHERE query_run_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, queryRunId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapQueryRunRow(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RepositoryException("Could not find query run by id: " + queryRunId, e);
        }
    }

    public List<QueryRunRow> findAllQueryRuns() {
        String sql = """
                SELECT query_run_id, query_text, similarity_metric, n_value, created_at
                FROM query_runs
                ORDER BY query_run_id
                """;

        List<QueryRunRow> queryRuns = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                queryRuns.add(mapQueryRunRow(resultSet));
            }

            return queryRuns;

        } catch (SQLException e) {
            throw new RepositoryException("Could not list query runs.", e);
        }
    }

    public void saveQueryResult(
            long queryRunId,
            int rankPosition,
            long documentId,
            double score
    ) {
        String sql = """
                INSERT INTO query_results (query_run_id, rank_position, document_id, score)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (query_run_id, rank_position)
                DO UPDATE SET
                    document_id = EXCLUDED.document_id,
                    score = EXCLUDED.score
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, queryRunId);
            statement.setInt(2, rankPosition);
            statement.setLong(3, documentId);
            statement.setDouble(4, score);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not save query result for queryRunId=" + queryRunId
                            + ", rankPosition=" + rankPosition,
                    e
            );
        }
    }

    public List<QueryResultRow> findResultsByQueryRunId(long queryRunId) {
        String sql = """
                SELECT query_run_id, rank_position, document_id, score
                FROM query_results
                WHERE query_run_id = ?
                ORDER BY rank_position
                """;

        List<QueryResultRow> results = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, queryRunId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapQueryResultRow(resultSet));
                }
            }

            return results;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not find query results for queryRunId=" + queryRunId,
                    e
            );
        }
    }

    public List<QueryResultRow> findAllQueryResults() {
        String sql = """
                SELECT query_run_id, rank_position, document_id, score
                FROM query_results
                ORDER BY query_run_id, rank_position
                """;

        List<QueryResultRow> results = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                results.add(mapQueryResultRow(resultSet));
            }

            return results;

        } catch (SQLException e) {
            throw new RepositoryException("Could not list query results.", e);
        }
    }

    public int deleteAllQueryResults() {
        String sql = "DELETE FROM query_results";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            return statement.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Could not delete query results.", e);
        }
    }

    public int deleteAllQueryRuns() {
        String sql = "DELETE FROM query_runs";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            return statement.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Could not delete query runs.", e);
        }
    }

    public void deleteAll() {
        deleteAllQueryResults();
        deleteAllQueryRuns();
    }

    private QueryRunRow mapQueryRunRow(ResultSet resultSet) throws SQLException {
        return new QueryRunRow(
                resultSet.getLong("query_run_id"),
                resultSet.getString("query_text"),
                resultSet.getString("similarity_metric"),
                resultSet.getInt("n_value"),
                resultSet.getTimestamp("created_at")
        );
    }

    private QueryResultRow mapQueryResultRow(ResultSet resultSet) throws SQLException {
        return new QueryResultRow(
                resultSet.getLong("query_run_id"),
                resultSet.getInt("rank_position"),
                resultSet.getLong("document_id"),
                resultSet.getDouble("score")
        );
    }

    public record QueryRunRow(
            long queryRunId,
            String queryText,
            String similarityMetric,
            int nValue,
            Timestamp createdAt
    ) {
    }

    public record QueryResultRow(
            long queryRunId,
            int rankPosition,
            long documentId,
            double score
    ) {
    }

    public static class RepositoryException extends RuntimeException {
        public RepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
