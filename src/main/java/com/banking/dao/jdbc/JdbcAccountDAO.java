package com.banking.dao.jdbc;

import com.banking.dao.AccountDAO;
import com.banking.model.Account;
import com.banking.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcAccountDAO implements AccountDAO {
    private static final String COLUMNS = "account_id, user_id, account_number, account_type, balance, status";

    @Override
    public void createAccount(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (user_id, account_number, account_type, balance, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setCreateParameters(statement, account);
            statement.executeUpdate();
        }
    }

    @Override
    public Account findByAccountNumber(String accountNumber) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS + " FROM accounts WHERE account_number = ?")) {
            statement.setString(1, accountNumber);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? map(results) : null;
            }
        }
    }

    @Override
    public Account findById(int accountId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS + " FROM accounts WHERE account_id = ?")) {
            statement.setInt(1, accountId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? map(results) : null;
            }
        }
    }

    @Override
    public List<Account> findByUserId(int userId) throws SQLException {
        return findList("SELECT " + COLUMNS + " FROM accounts WHERE user_id = ?", userId);
    }

    @Override
    public void updateBalance(int accountId, BigDecimal balance) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            updateBalance(connection, accountId, balance);
        }
    }

    @Override
    public void updateBalance(Connection connection, int accountId, BigDecimal balance) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE accounts SET balance = ? WHERE account_id = ?")) {
            statement.setBigDecimal(1, balance);
            statement.setInt(2, accountId);
            if (statement.executeUpdate() != 1) throw new SQLException("Account balance was not updated.");
        }
    }

    @Override
    public void updateStatus(int accountId, String status) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE accounts SET status = ? WHERE account_id = ?")) {
            statement.setString(1, status);
            statement.setInt(2, accountId);
            if (statement.executeUpdate() != 1) throw new SQLException("Account status was not updated.");
        }
    }

    @Override
    public List<Account> findAll() throws SQLException {
        return findList("SELECT " + COLUMNS + " FROM accounts ORDER BY account_id", null);
    }

    private List<Account> findList(String sql, Integer userId) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (userId != null) statement.setInt(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) accounts.add(map(results));
            }
        }
        return accounts;
    }

    private void setCreateParameters(PreparedStatement statement, Account account) throws SQLException {
        statement.setInt(1, account.getUserId());
        statement.setString(2, account.getAccountNumber());
        statement.setString(3, account.getAccountType());
        statement.setBigDecimal(4, account.getBalance());
        statement.setString(5, account.getStatus());
    }

    private Account map(ResultSet results) throws SQLException {
        return new Account(results.getInt("account_id"), results.getInt("user_id"),
                results.getString("account_number"), results.getString("account_type"),
                results.getBigDecimal("balance"), results.getString("status"));
    }
}