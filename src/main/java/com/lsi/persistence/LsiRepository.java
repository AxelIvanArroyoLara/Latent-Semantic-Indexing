package com.lsi.persistence;

import com.lsi.config.DatabaseConfig;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing LSI persistence tables.
 *
 * This class stores and retrieves latent models, document vectors,
 * and query vectors. It does not calculate SVD or LSI projections.
 */
public class LsiRepository {

    private final DataSource dataSource;

    public LsiRepository() {
        this(DatabaseConfig.getDataSource());
    }

    public LsiRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long saveLatentModel(
            int kValue,
            String sourceMatrixType,
            String notes
    ) {
        String sql = """
                INSERT INTO latent_models (k_value, source_matrix_type, notes)
                VALUES (?, ?, ?)
                RETURNING latent_model_id
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, kValue);
            statement.setString(2, sourceMatrixType);
            statement.setString(3, notes);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("latent_model_id");
                }
            }

            throw new SQLException("Latent model insert did not return an id.");

        } catch (SQLException e) {
            throw new RepositoryException("Could not save latent model.", e);
        }
    }

    public Optional<LatentModelRow> findLatentModelById(long latentModelId) {
        String sql = """
                SELECT latent_model_id, k_value, source_matrix_type, created_at, notes
                FROM latent_models
                WHERE latent_model_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, latentModelId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapLatentModelRow(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RepositoryException("Could not find latent model by id: " + latentModelId, e);
        }
    }

    public List<LatentModelRow> findAllLatentModels() {
        String sql = """
                SELECT latent_model_id, k_value, source_matrix_type, created_at, notes
                FROM latent_models
                ORDER BY latent_model_id
                """;

        List<LatentModelRow> models = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                models.add(mapLatentModelRow(resultSet));
            }

            return models;

        } catch (SQLException e) {
            throw new RepositoryException("Could not list latent models.", e);
        }
    }

    public void saveDocumentVectorComponent(
            long latentModelId,
            long documentId,
            int componentIndex,
            double componentValue
    ) {
        String sql = """
                INSERT INTO latent_document_vectors
                    (latent_model_id, document_id, component_index, component_value)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (latent_model_id, document_id, component_index)
                DO UPDATE SET
                    component_value = EXCLUDED.component_value
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, latentModelId);
            statement.setLong(2, documentId);
            statement.setInt(3, componentIndex);
            statement.setDouble(4, componentValue);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not save document vector component for latentModelId="
                            + latentModelId + ", documentId=" + documentId,
                    e
            );
        }
    }

    public List<LatentDocumentVectorRow> findDocumentVector(
            long latentModelId,
            long documentId
    ) {
        String sql = """
                SELECT latent_model_id, document_id, component_index, component_value
                FROM latent_document_vectors
                WHERE latent_model_id = ?
                  AND document_id = ?
                ORDER BY component_index
                """;

        List<LatentDocumentVectorRow> vector = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, latentModelId);
            statement.setLong(2, documentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    vector.add(mapDocumentVectorRow(resultSet));
                }
            }

            return vector;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not find document vector for latentModelId="
                            + latentModelId + ", documentId=" + documentId,
                    e
            );
        }
    }

    public List<LatentDocumentVectorRow> findDocumentVectorsByModel(long latentModelId) {
        String sql = """
                SELECT latent_model_id, document_id, component_index, component_value
                FROM latent_document_vectors
                WHERE latent_model_id = ?
                ORDER BY document_id, component_index
                """;

        List<LatentDocumentVectorRow> vectors = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, latentModelId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    vectors.add(mapDocumentVectorRow(resultSet));
                }
            }

            return vectors;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not find document vectors for latentModelId=" + latentModelId,
                    e
            );
        }
    }

    public long saveQueryVectorComponent(
            long latentModelId,
            String queryText,
            int componentIndex,
            double componentValue
    ) {
        String sql = """
                INSERT INTO latent_query_vectors
                    (latent_model_id, query_text, component_index, component_value)
                VALUES (?, ?, ?, ?)
                RETURNING latent_query_vector_id
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, latentModelId);
            statement.setString(2, queryText);
            statement.setInt(3, componentIndex);
            statement.setDouble(4, componentValue);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("latent_query_vector_id");
                }
            }

            throw new SQLException("Query vector component insert did not return an id.");

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not save query vector component for latentModelId=" + latentModelId,
                    e
            );
        }
    }

    public List<LatentQueryVectorRow> findQueryVectorsByModel(long latentModelId) {
        String sql = """
                SELECT latent_query_vector_id, latent_model_id, query_text,
                       component_index, component_value, created_at
                FROM latent_query_vectors
                WHERE latent_model_id = ?
                ORDER BY latent_query_vector_id, component_index
                """;

        List<LatentQueryVectorRow> vectors = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, latentModelId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    vectors.add(mapQueryVectorRow(resultSet));
                }
            }

            return vectors;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not find query vectors for latentModelId=" + latentModelId,
                    e
            );
        }
    }

    public int deleteAllDocumentVectors() {
        String sql = "DELETE FROM latent_document_vectors";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            return statement.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Could not delete latent document vectors.", e);
        }
    }

    public int deleteAllQueryVectors() {
        String sql = "DELETE FROM latent_query_vectors";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            return statement.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Could not delete latent query vectors.", e);
        }
    }

    public int deleteAllLatentModels() {
        String sql = "DELETE FROM latent_models";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            return statement.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Could not delete latent models.", e);
        }
    }

    public void deleteAll() {
        deleteAllDocumentVectors();
        deleteAllQueryVectors();
        deleteAllLatentModels();
    }

    private LatentModelRow mapLatentModelRow(ResultSet resultSet) throws SQLException {
        return new LatentModelRow(
                resultSet.getLong("latent_model_id"),
                resultSet.getInt("k_value"),
                resultSet.getString("source_matrix_type"),
                resultSet.getTimestamp("created_at"),
                resultSet.getString("notes")
        );
    }

    private LatentDocumentVectorRow mapDocumentVectorRow(ResultSet resultSet) throws SQLException {
        return new LatentDocumentVectorRow(
                resultSet.getLong("latent_model_id"),
                resultSet.getLong("document_id"),
                resultSet.getInt("component_index"),
                resultSet.getDouble("component_value")
        );
    }

    private LatentQueryVectorRow mapQueryVectorRow(ResultSet resultSet) throws SQLException {
        return new LatentQueryVectorRow(
                resultSet.getLong("latent_query_vector_id"),
                resultSet.getLong("latent_model_id"),
                resultSet.getString("query_text"),
                resultSet.getInt("component_index"),
                resultSet.getDouble("component_value"),
                resultSet.getTimestamp("created_at")
        );
    }

    public record LatentModelRow(
            long latentModelId,
            int kValue,
            String sourceMatrixType,
            Timestamp createdAt,
            String notes
    ) {
    }

    public record LatentDocumentVectorRow(
            long latentModelId,
            long documentId,
            int componentIndex,
            double componentValue
    ) {
    }

    public record LatentQueryVectorRow(
            long latentQueryVectorId,
            long latentModelId,
            String queryText,
            int componentIndex,
            double componentValue,
            Timestamp createdAt
    ) {
    }

    public static class RepositoryException extends RuntimeException {
        public RepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}