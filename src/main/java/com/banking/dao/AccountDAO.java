package com.banking.dao;

import com.banking.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface AccountDAO {
    void createAccount(Account account) throws SQLException;
    Account findByAccountNumber(String accountNumber) throws SQLException;
    Account findById(int accountId) throws SQLException;
    List<Account> findByUserId(int userId) throws SQLException;
    void updateBalance(int accountId, BigDecimal balance) throws SQLException;
    void updateBalance(Connection connection, int accountId, BigDecimal balance) throws SQLException;
    void updateStatus(int accountId, String status) throws SQLException;
    List<Account> findAll() throws SQLException;
}