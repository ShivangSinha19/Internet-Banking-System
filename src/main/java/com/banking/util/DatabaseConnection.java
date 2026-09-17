package com.banking.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/internet_banking";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = setting("DB_URL", DEFAULT_URL);
        String username = setting("DB_USERNAME", null);
        String password = setting("DB_PASSWORD", null);
        if (username == null || password == null) {
            throw new SQLException("DB_USERNAME and DB_PASSWORD environment variables are required.");
        }
        return DriverManager.getConnection(url, username, password);
    }

    private static String setting(String name, String defaultValue) {
        String environmentValue = System.getenv(name);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }
        String propertyValue = System.getProperty(name);
        return propertyValue == null || propertyValue.isBlank() ? defaultValue : propertyValue;
    }
}