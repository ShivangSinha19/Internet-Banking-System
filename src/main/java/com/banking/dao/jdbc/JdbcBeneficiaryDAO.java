package com.banking.dao.jdbc;

import com.banking.dao.BeneficiaryDAO;
import com.banking.model.Beneficiary;
import com.banking.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcBeneficiaryDAO implements BeneficiaryDAO {
    private static final String COLUMNS = "beneficiary_id, user_id, name, account_number, bank_name, created_at";

    @Override
    public void createBeneficiary(Beneficiary beneficiary) throws SQLException {
        String sql = "INSERT INTO beneficiaries (user_id, name, account_number, bank_name, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, beneficiary.getUserId());
            statement.setString(2, beneficiary.getName());
            statement.setString(3, beneficiary.getAccountNumber());
            statement.setString(4, beneficiary.getBankName());
            statement.setTimestamp(5, java.sql.Timestamp.valueOf(beneficiary.getCreatedAt()));
            statement.executeUpdate();
        }
    }

    @Override
    public List<Beneficiary> findByUserId(int userId) throws SQLException {
        List<Beneficiary> beneficiaries = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS + " FROM beneficiaries WHERE user_id = ? ORDER BY beneficiary_id")) {
            statement.setInt(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) beneficiaries.add(map(results));
            }
        }
        return beneficiaries;
    }

    @Override
    public void deleteBeneficiary(int userId, int beneficiaryId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM beneficiaries WHERE user_id = ? AND beneficiary_id = ?")) {
            statement.setInt(1, userId);
            statement.setInt(2, beneficiaryId);
            statement.executeUpdate();
        }
    }

    private Beneficiary map(ResultSet results) throws SQLException {
        return new Beneficiary(results.getInt("beneficiary_id"), results.getInt("user_id"),
                results.getString("name"), results.getString("account_number"),
                results.getString("bank_name"), results.getTimestamp("created_at").toLocalDateTime());
    }
}