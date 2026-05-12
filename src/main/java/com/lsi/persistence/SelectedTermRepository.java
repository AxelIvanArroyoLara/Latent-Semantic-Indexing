package com.lsi.persistence;

import com.lsi.config.DatabaseConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SelectedTermRepository {

    private final DataSource dataSource;

    public SelectedTermRepository() {
        this(DatabaseConfig.getDataSource());
    }

    public SelectedTermRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void saveSelectedTerm(
            Long latentModelId,
            Long termId,
            String termText,
            double significance,
            String selectedBy,
            String selectionSource
    ) {
        String sql = """
                INSERT INTO selected_index_terms
                    (latent_model_id, term_id, term_text, significance, selected_by, selection_source)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (latentModelId == null) {
                statement.setNull(1, java.sql.Types.BIGINT);
            } else {
                statement.setLong(1, latentModelId);
            }

            if (termId == null) {
                statement.setNull(2, java.sql.Types.BIGINT);
            } else {
                statement.setLong(2, termId);
            }

            statement.setString(3, termText);
            statement.setDouble(4, significance);
            statement.setString(5, selectedBy);
            statement.setString(6, selectionSource);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Could not save selected index term: " + termText, e);
        }
    }

    public static class RepositoryException extends RuntimeException {
        public RepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
