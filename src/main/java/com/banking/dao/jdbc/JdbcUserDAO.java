package com.banking.dao.jdbc;

import com.banking.dao.UserDAO;
import com.banking.model.User;
import com.banking.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcUserDAO implements UserDAO {
    private static final String COLUMNS = "user_id, first_name, last_name, email, password_hash, role";

    @Override
    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (first_name, last_name, email, password_hash, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPassword());
            statement.setString(5, user.getRole());
            statement.executeUpdate();
        }
    }

    @Override
    public User findByEmail(String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return findOne(connection, "SELECT " + COLUMNS + " FROM users WHERE email = ?", email);
        }
    }

    @Override
    public User findById(int userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return findOne(connection, "SELECT " + COLUMNS + " FROM users WHERE user_id = ?", userId);
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS + " FROM users ORDER BY user_id");
             ResultSet results = statement.executeQuery()) {
            while (results.next()) users.add(map(results));
        }
        return users;
    }

    private User findOne(Connection connection, String sql, Object value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (value instanceof Integer) statement.setInt(1, (Integer) value);
            else statement.setString(1, (String) value);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? map(results) : null;
            }
        }
    }

    private User map(ResultSet results) throws SQLException {
        return new User(results.getInt("user_id"), results.getString("first_name"),
                results.getString("last_name"), results.getString("email"),
                results.getString("password_hash"), results.getString("role"));
    }
}