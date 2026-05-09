package com.lsi.persistence;

import com.lsi.config.DatabaseConfig;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing the documents table.
 *
 * This class only handles persistence operations.
 * It does not preprocess text, tokenize content, calculate frequencies, or run LSI.
 */
public class DocumentRepository {

    private final DataSource dataSource;

    public DocumentRepository() {
        this(DatabaseConfig.getDataSource());
    }

    public DocumentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long save(
            String code,
            String title,
            String source,
            String rawText,
            String normalizedText,
            String languageCode
    ) {
        String sql = """
                INSERT INTO documents (code, title, source, raw_text, normalized_text, language_code)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING document_id
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, code);
            statement.setString(2, title);
            statement.setString(3, source);
            statement.setString(4, rawText);
            statement.setString(5, normalizedText);
            statement.setString(6, languageCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("document_id");
                }
            }

            throw new SQLException("Document insert did not return an id.");

        } catch (SQLException e) {
            throw new RepositoryException("Could not save document with code: " + code, e);
        }
    }

    public Optional<DocumentRow> findById(long documentId) {
        String sql = """
                SELECT document_id, code, title, source, raw_text, normalized_text, language_code, created_at
                FROM documents
                WHERE document_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, documentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RepositoryException("Could not find document by id: " + documentId, e);
        }
    }

    public Optional<DocumentRow> findByCode(String code) {
        String sql = """
                SELECT document_id, code, title, source, raw_text, normalized_text, language_code, created_at
                FROM documents
                WHERE code = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RepositoryException("Could not find document by code: " + code, e);
        }
    }

    public List<DocumentRow> findAll() {
        String sql = """
                SELECT document_id, code, title, source, raw_text, normalized_text, language_code, created_at
                FROM documents
                ORDER BY document_id
                """;

        List<DocumentRow> documents = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                documents.add(mapRow(resultSet));
            }

            return documents;

        } catch (SQLException e) {
            throw new RepositoryException("Could not list documents.", e);
        }
    }

    public int deleteAll() {
        String sql = "DELETE FROM documents";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            return statement.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Could not delete documents.", e);
        }
    }

    private DocumentRow mapRow(ResultSet resultSet) throws SQLException {
        return new DocumentRow(
                resultSet.getLong("document_id"),
                resultSet.getString("code"),
                resultSet.getString("title"),
                resultSet.getString("source"),
                resultSet.getString("raw_text"),
                resultSet.getString("normalized_text"),
                resultSet.getString("language_code"),
                resultSet.getTimestamp("created_at")
        );
    }

    public record DocumentRow(
            long documentId,
            String code,
            String title,
            String source,
            String rawText,
            String normalizedText,
            String languageCode,
            Timestamp createdAt
    ) {
    }

    public static class RepositoryException extends RuntimeException {
        public RepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}