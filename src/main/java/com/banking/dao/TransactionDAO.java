package com.banking.dao;

import com.banking.model.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface TransactionDAO {
    void createTransaction(Transaction transaction) throws SQLException;
    void createTransaction(Connection connection, Transaction transaction) throws SQLException;
    List<Transaction> findByAccountId(int accountId) throws SQLException;
    List<Transaction> findAll() throws SQLException;
}