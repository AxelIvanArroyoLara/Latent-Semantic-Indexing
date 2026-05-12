package com.lsi.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseConfigTest {

    @Test
    void shouldOpenDatabaseConnection() throws SQLException {
        try {
            DatabaseConfig.getConnection().close();
        } catch (Exception e) {
            Assumptions.assumeTrue(
                    false,
                    "PostgreSQL test database is not available: " + e.getMessage()
            );
        }

        try (Connection connection = DatabaseConfig.getConnection()) {
            assertNotNull(connection, "Connection should not be null");
            assertFalse(connection.isClosed(), "Connection should be open");
        }
    }
}
