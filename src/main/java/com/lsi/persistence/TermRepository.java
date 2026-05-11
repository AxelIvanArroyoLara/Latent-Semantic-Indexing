package com.lsi.persistence;

import com.lsi.config.DatabaseConfig;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing the terms table.
 *
 * This class only handles persistence operations for vocabulary terms.
 * It does not perform stemming, synonym expansion, polysemy resolution,
 * frequency calculation, or LSI operations.
 */
public class TermRepository {

    private final DataSource dataSource;

    public TermRepository() {
        this(DatabaseConfig.getDataSource());
    }

    public TermRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long save(
            String normalizedTerm,
            String canonicalTerm,
            String stem,
            String senseLabel
    ) {
        String sql = """
                INSERT INTO terms (normalized_term, canonical_term, stem, sense_label)
                VALUES (?, ?, ?, ?)
                RETURNING term_id
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedTerm);
            statement.setString(2, canonicalTerm);
            statement.setString(3, stem);
            statement.setString(4, senseLabel);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("term_id");
                }
            }

            throw new SQLException("Term insert did not return an id.");

        } catch (SQLException e) {
            throw new RepositoryException("Could not save term: " + normalizedTerm, e);
        }
    }

    public Optional<TermRow> findById(long termId) {
        String sql = """
                SELECT term_id, normalized_term, canonical_term, stem, sense_label
                FROM terms
                WHERE term_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, termId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RepositoryException("Could not find term by id: " + termId, e);
        }
    }

    public Optional<TermRow> findByNormalizedTerm(String normalizedTerm) {
        String sql = """
                SELECT term_id, normalized_term, canonical_term, stem, sense_label
                FROM terms
                WHERE normalized_term = ?
                ORDER BY term_id
                LIMIT 1
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedTerm);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RepositoryException("Could not find term by normalized term: " + normalizedTerm, e);
        }
    }

    public Optional<TermRow> findByNormalizedTermAndSense(
            String normalizedTerm,
            String senseLabel
    ) {
        String sql = """
                SELECT term_id, normalized_term, canonical_term, stem, sense_label
                FROM terms
                WHERE normalized_term = ?
                  AND (
                        (? IS NULL AND sense_label IS NULL)
                        OR sense_label = ?
                  )
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedTerm);
            statement.setString(2, senseLabel);
            statement.setString(3, senseLabel);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not find term by normalized term and sense: " + normalizedTerm,
                    e
            );
        }
    }

    public List<TermRow> findAll() {
        String sql = """
                SELECT term_id, normalized_term, canonical_term, stem, sense_label
                FROM terms
                ORDER BY term_id
                """;

        List<TermRow> terms = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                terms.add(mapRow(resultSet));
            }

            return terms;

        } catch (SQLException e) {
            throw new RepositoryException("Could not list terms.", e);
        }
    }

    public int deleteAll() {
        String sql = "DELETE FROM terms";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            return statement.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Could not delete terms.", e);
        }
    }

    private TermRow mapRow(ResultSet resultSet) throws SQLException {
        return new TermRow(
                resultSet.getLong("term_id"),
                resultSet.getString("normalized_term"),
                resultSet.getString("canonical_term"),
                resultSet.getString("stem"),
                resultSet.getString("sense_label")
        );
    }

    public record TermRow(
            long termId,
            String normalizedTerm,
            String canonicalTerm,
            String stem,
            String senseLabel
    ) {
    }

    public static class RepositoryException extends RuntimeException {
        public RepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
