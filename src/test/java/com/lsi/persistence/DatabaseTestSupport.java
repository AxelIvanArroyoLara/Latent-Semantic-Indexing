package com.lsi.persistence;

import com.lsi.config.DatabaseConfig;
import org.junit.jupiter.api.Assumptions;

import java.sql.Connection;

final class DatabaseTestSupport {

    private DatabaseTestSupport() {
        // Test utility.
    }

    static void assumeDatabaseAvailable() {
        try (Connection ignored = DatabaseConfig.getConnection()) {
            // Connection opened successfully.
        } catch (Exception e) {
            Assumptions.assumeTrue(
                    false,
                    "PostgreSQL test database is not available: " + e.getMessage()
            );
        }
    }
}
