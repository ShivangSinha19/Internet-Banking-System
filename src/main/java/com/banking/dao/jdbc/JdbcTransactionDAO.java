package com.banking.dao.jdbc;

import com.banking.dao.TransactionDAO;
import com.banking.model.Transaction;
import com.banking.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JdbcTransactionDAO implements TransactionDAO {
    private static final String COLUMNS = "transaction_id, from_account_id, to_account_id, amount, transaction_type, transaction_date, description";

    @Override
    public void createTransaction(Transaction transaction) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            createTransaction(connection, transaction);
        }
    }

    @Override
    public void createTransaction(Connection connection, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (from_account_id, to_account_id, amount, transaction_type, transaction_date, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (transaction.getFromAccountId() == 0) statement.setNull(1, java.sql.Types.INTEGER);
            else statement.setInt(1, transaction.getFromAccountId());
            if (transaction.getToAccountId() == 0) statement.setNull(2, java.sql.Types.INTEGER);
            else statement.setInt(2, transaction.getToAccountId());
            statement.setBigDecimal(3, transaction.getAmount());
            statement.setString(4, transaction.getTransactionType());
            statement.setTimestamp(5, Timestamp.valueOf(transaction.getTransactionDate()));
            statement.setString(6, transaction.getDescription());
            statement.executeUpdate();
        }
    }

    @Override
    public List<Transaction> findByAccountId(int accountId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM transactions WHERE from_account_id = ? OR to_account_id = ? ORDER BY transaction_date";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, accountId);
            statement.setInt(2, accountId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) transactions.add(map(results));
            }
        }
        return transactions;
    }

    @Override
    public List<Transaction> findAll() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS + " FROM transactions ORDER BY transaction_date");
             ResultSet results = statement.executeQuery()) {
            while (results.next()) transactions.add(map(results));
        }
        return transactions;
    }

    private Transaction map(ResultSet results) throws SQLException {
        int fromAccountId = results.getObject("from_account_id", Integer.class) == null
                ? 0 : results.getInt("from_account_id");
        int toAccountId = results.getObject("to_account_id", Integer.class) == null
                ? 0 : results.getInt("to_account_id");
        return new Transaction(results.getInt("transaction_id"), fromAccountId, toAccountId,
                results.getBigDecimal("amount"), results.getString("transaction_type"),
                results.getTimestamp("transaction_date").toLocalDateTime(), results.getString("description"));
    }
}