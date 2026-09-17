package com.banking.service;

import com.banking.dao.AccountDAO;
import com.banking.dao.BeneficiaryDAO;
import com.banking.dao.TransactionDAO;
import com.banking.exception.AccountFrozenException;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.InsufficientBalanceException;
import com.banking.exception.InvalidAmountException;
import com.banking.exception.InvalidTransferException;
import com.banking.model.Account;
import com.banking.model.Beneficiary;
import com.banking.model.Transaction;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class JdbcBankingService {
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final BeneficiaryDAO beneficiaryDAO;
    private final JdbcTransferService transferService;

    public JdbcBankingService(AccountDAO accountDAO, TransactionDAO transactionDAO,
                              BeneficiaryDAO beneficiaryDAO) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
        this.beneficiaryDAO = beneficiaryDAO;
        this.transferService = new JdbcTransferService(accountDAO, transactionDAO);
    }

    public Account createAccount(Account account) throws SQLException {
        accountDAO.createAccount(account);
        return accountDAO.findByAccountNumber(account.getAccountNumber());
    }

    public void deposit(String accountNumber, BigDecimal amount)
            throws InvalidAmountException, AccountNotFoundException,
            AccountFrozenException, SQLException {
        validateAmount(amount);
        Account account = activeAccount(accountNumber);
        accountDAO.updateBalance(account.getAccountId(), account.getBalance().add(amount));
        transactionDAO.createTransaction(new Transaction(0, account.getAccountId(), 0,
                amount, "DEPOSIT", LocalDateTime.now(), "Cash deposit"));
    }

    public void withdraw(String accountNumber, BigDecimal amount)
            throws InvalidAmountException, AccountNotFoundException,
            AccountFrozenException, InsufficientBalanceException, SQLException {
        validateAmount(amount);
        Account account = activeAccount(accountNumber);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance.");
        }
        accountDAO.updateBalance(account.getAccountId(), account.getBalance().subtract(amount));
        transactionDAO.createTransaction(new Transaction(0, account.getAccountId(), 0,
                amount, "WITHDRAW", LocalDateTime.now(), "Cash withdrawal"));
    }

    public void transfer(String senderNumber, String receiverNumber, BigDecimal amount)
            throws InvalidAmountException, AccountNotFoundException,
            AccountFrozenException, InsufficientBalanceException,
            InvalidTransferException, SQLException {
        transferService.transfer(senderNumber, receiverNumber, amount);
    }

    public List<Transaction> getTransactionsForAccount(String accountNumber)
            throws AccountNotFoundException, SQLException {
        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) throw new AccountNotFoundException("Account was not found.");
        return transactionDAO.findByAccountId(account.getAccountId());
    }

    public List<Transaction> getAllTransactions() throws SQLException {
        return transactionDAO.findAll();
    }

    public void freezeAccount(String accountNumber) throws AccountNotFoundException, SQLException {
        Account account = account(accountNumber);
        accountDAO.updateStatus(account.getAccountId(), "FROZEN");
    }

    public void unfreezeAccount(String accountNumber) throws AccountNotFoundException, SQLException {
        Account account = account(accountNumber);
        accountDAO.updateStatus(account.getAccountId(), "ACTIVE");
    }

    public Beneficiary addBeneficiary(Beneficiary beneficiary) throws SQLException {
        beneficiaryDAO.createBeneficiary(beneficiary);
        return beneficiary;
    }

    public List<Beneficiary> getBeneficiariesForUser(int userId) throws SQLException {
        return beneficiaryDAO.findByUserId(userId);
    }

    public void removeBeneficiary(int userId, int beneficiaryId) throws SQLException {
        beneficiaryDAO.deleteBeneficiary(userId, beneficiaryId);
    }

    private Account activeAccount(String accountNumber)
            throws AccountNotFoundException, AccountFrozenException, SQLException {
        Account account = account(accountNumber);
        if (!account.isActive()) throw new AccountFrozenException("Account is frozen.");
        return account;
    }

    private Account account(String accountNumber) throws AccountNotFoundException, SQLException {
        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) throw new AccountNotFoundException("Account was not found.");
        return account;
    }

    private void validateAmount(BigDecimal amount) throws InvalidAmountException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
    }
}