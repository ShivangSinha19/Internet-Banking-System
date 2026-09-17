package com.banking;

import com.banking.util.DatabaseConnection;

import java.sql.Connection;

public class TestDatabaseConnection {

    public static void main(String[] args) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection != null && !connection.isClosed()) {
                System.out.println("MySQL connection successful!");
            }

        } catch (Exception e) {
            System.out.println("Database connection failed.");
            e.printStackTrace();
        }
    }
}
