package com.banking.service;

import com.banking.dao.AccountDAO;
import com.banking.dao.TransactionDAO;
import com.banking.exception.AccountFrozenException;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.InsufficientBalanceException;
import com.banking.exception.InvalidAmountException;
import com.banking.exception.InvalidTransferException;
import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class JdbcTransferService {
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    public JdbcTransferService(AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
    }

    public void transfer(String senderNumber, String receiverNumber, BigDecimal amount)
            throws InvalidAmountException, AccountNotFoundException, AccountFrozenException,
            InsufficientBalanceException, InvalidTransferException, SQLException {
        validateAmount(amount);
        if (blank(senderNumber) || blank(receiverNumber)
                || senderNumber.trim().equalsIgnoreCase(receiverNumber.trim())) {
            throw new InvalidTransferException("Sender and receiver must be different valid accounts.");
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Account sender = accountDAO.findByAccountNumber(senderNumber);
                Account receiver = accountDAO.findByAccountNumber(receiverNumber);
                validateAccounts(sender, receiver);
                if (sender.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientBalanceException("Insufficient balance for transfer.");
                }
                accountDAO.updateBalance(connection, sender.getAccountId(), sender.getBalance().subtract(amount));
                accountDAO.updateBalance(connection, receiver.getAccountId(), receiver.getBalance().add(amount));
                transactionDAO.createTransaction(connection, new Transaction(0, sender.getAccountId(),
                        receiver.getAccountId(), amount, "TRANSFER", LocalDateTime.now(),
                        "Transfer to " + receiver.getAccountNumber()));
                connection.commit();
            } catch (Exception exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                if (exception instanceof InvalidAmountException invalidAmount) throw invalidAmount;
                if (exception instanceof AccountNotFoundException missingAccount) throw missingAccount;
                if (exception instanceof AccountFrozenException frozenAccount) throw frozenAccount;
                if (exception instanceof InsufficientBalanceException insufficient) throw insufficient;
                if (exception instanceof InvalidTransferException invalidTransfer) throw invalidTransfer;
                if (exception instanceof SQLException sqlException) throw sqlException;
                throw new SQLException("Transfer failed.", exception);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void validateAccounts(Account sender, Account receiver)
            throws AccountNotFoundException, AccountFrozenException {
        if (sender == null || receiver == null) throw new AccountNotFoundException("Sender or receiver account was not found.");
        if (!sender.isActive() || !receiver.isActive()) throw new AccountFrozenException("Both accounts must be active.");
    }

    private void validateAmount(BigDecimal amount) throws InvalidAmountException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}