package com.lsi.persistence;

import com.lsi.config.DatabaseConfig;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing the document_terms table.
 *
 * This class persists the relationship between documents and terms.
 * It represents the frequency matrix FrecT in relational form.
 *
 * It does not calculate frequencies, tokenize text, preprocess documents,
 * or execute LSI operations.
 */
public class FrequencyRepository {

    private final DataSource dataSource;

    public FrequencyRepository() {
        this(DatabaseConfig.getDataSource());
    }

    public FrequencyRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void saveFrequency(
            long documentId,
            long termId,
            double rawFrequency,
            double weightedFrequency
    ) {
        String sql = """
                INSERT INTO document_terms (document_id, term_id, raw_frequency, weighted_frequency)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (document_id, term_id)
                DO UPDATE SET
                    raw_frequency = EXCLUDED.raw_frequency,
                    weighted_frequency = EXCLUDED.weighted_frequency
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, documentId);
            statement.setLong(2, termId);
            statement.setDouble(3, rawFrequency);
            statement.setDouble(4, weightedFrequency);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not save frequency for documentId=" + documentId + ", termId=" + termId,
                    e
            );
        }
    }

    public Optional<FrequencyRow> findByDocumentIdAndTermId(long documentId, long termId) {
        String sql = """
                SELECT document_id, term_id, raw_frequency, weighted_frequency
                FROM document_terms
                WHERE document_id = ?
                  AND term_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, documentId);
            statement.setLong(2, termId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not find frequency for documentId=" + documentId + ", termId=" + termId,
                    e
            );
        }
    }

    public List<FrequencyRow> findByDocumentId(long documentId) {
        String sql = """
                SELECT document_id, term_id, raw_frequency, weighted_frequency
                FROM document_terms
                WHERE document_id = ?
                ORDER BY term_id
                """;

        List<FrequencyRow> frequencies = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, documentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    frequencies.add(mapRow(resultSet));
                }
            }

            return frequencies;

        } catch (SQLException e) {
            throw new RepositoryException("Could not list frequencies for documentId=" + documentId, e);
        }
    }

    public List<FrequencyRow> findByTermId(long termId) {
        String sql = """
                SELECT document_id, term_id, raw_frequency, weighted_frequency
                FROM document_terms
                WHERE term_id = ?
                ORDER BY document_id
                """;

        List<FrequencyRow> frequencies = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, termId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    frequencies.add(mapRow(resultSet));
                }
            }

            return frequencies;

        } catch (SQLException e) {
            throw new RepositoryException("Could not list frequencies for termId=" + termId, e);
        }
    }

    public List<FrequencyRow> findAll() {
        String sql = """
                SELECT document_id, term_id, raw_frequency, weighted_frequency
                FROM document_terms
                ORDER BY document_id, term_id
                """;

        List<FrequencyRow> frequencies = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                frequencies.add(mapRow(resultSet));
            }

            return frequencies;

        } catch (SQLException e) {
            throw new RepositoryException("Could not list all frequencies.", e);
        }
    }

    public int deleteAll() {
        String sql = "DELETE FROM document_terms";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            return statement.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Could not delete document-term frequencies.", e);
        }
    }

    private FrequencyRow mapRow(ResultSet resultSet) throws SQLException {
        return new FrequencyRow(
                resultSet.getLong("document_id"),
                resultSet.getLong("term_id"),
                resultSet.getDouble("raw_frequency"),
                resultSet.getDouble("weighted_frequency")
        );
    }

    public record FrequencyRow(
            long documentId,
            long termId,
            double rawFrequency,
            double weightedFrequency
    ) {
    }

    public static class RepositoryException extends RuntimeException {
        public RepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}