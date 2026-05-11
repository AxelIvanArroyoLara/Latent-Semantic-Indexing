package com.lsi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database configuration for the LSI document base system.
 *
 * This class loads the PostgreSQL connection settings from application.properties
 * and creates a HikariCP DataSource that can be reused by repositories.
 */
public final class DatabaseConfig {

    private static final String PROPERTIES_FILE = "application.properties";

    private static HikariDataSource dataSource;

    private DatabaseConfig() {
        // Utility class. Prevent instantiation.
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = createDataSource();
        }

        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private static HikariDataSource createDataSource() {
        Properties properties = loadProperties();

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Missing property: db.url");
        }

        if (user == null || user.isBlank()) {
            throw new IllegalStateException("Missing property: db.user");
        }

        if (password == null) {
            throw new IllegalStateException("Missing property: db.password");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);

        config.setPoolName("LsiDocumentBasePool");

        return new HikariDataSource(config);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream = DatabaseConfig.class
                .getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {

            if (inputStream == null) {
                throw new IllegalStateException("Could not find " + PROPERTIES_FILE + " in src/main/resources");
            }

            properties.load(inputStream);
            return properties;

        } catch (IOException e) {
            throw new IllegalStateException("Could not load " + PROPERTIES_FILE, e);
        }
    }
}
