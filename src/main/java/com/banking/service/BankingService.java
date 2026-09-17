package com.banking.service;

import com.banking.exception.AccountFrozenException;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.InsufficientBalanceException;
import com.banking.exception.InvalidAmountException;
import com.banking.exception.InvalidTransferException;
import com.banking.model.Account;
import com.banking.model.Beneficiary;
import com.banking.model.Transaction;
import com.banking.model.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class BankingService {
    private final UserService userService;
    private final List<Account> accounts = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private final List<Beneficiary> beneficiaries = new ArrayList<>();
    private int nextAccountId = 1;
    private int nextTransactionId = 1;
    private int nextBeneficiaryId = 1;

    public BankingService(UserService userService) {
        this.userService = userService;
    }

    // Create a new bank account
    public Account createAccount(int userId,
                                 String accountNumber,
                                 String accountType) {
        if (userService.findUserById(userId) == null) {
            throw new IllegalArgumentException("User does not exist.");
        }
        if (isBlank(accountNumber) || findAccountByNumber(accountNumber) != null) {
            throw new IllegalArgumentException("Account number is missing or already exists.");
        }
        if (isBlank(accountType)) {
            throw new IllegalArgumentException("Account type is required.");
        }
        Account account = new Account(
                nextAccountId,
                userId,
                accountNumber.trim(),
                accountType.trim().toUpperCase(),
                BigDecimal.ZERO,
                "ACTIVE"
        );
        accounts.add(account);
        nextAccountId++;
        return account;
    }
    // Find account by account number
    public Account findAccountByNumber(String accountNumber) {
        if (accountNumber == null) {
            return null;
        }
        for (Account account : accounts) {
            if (account.getAccountNumber().equalsIgnoreCase(accountNumber.trim())) {
                return account;
            }
        }
        return null;
    }

    public List<Account> getAccountsForUser(int userId) {
        List<Account> result = new ArrayList<>();
        for (Account account : accounts) {
            if (account.getUserId() == userId) {
                result.add(account);
            }
        }
        return result;
    }

    // Deposit money
    public void deposit(String accountNumber, BigDecimal amount)
            throws InvalidAmountException, AccountNotFoundException, AccountFrozenException {
        validateAmount(amount);
        Account account = getUsableAccount(accountNumber);
        account.setBalance(account.getBalance().add(amount));
        addTransaction(account.getAccountId(), 0, amount, "DEPOSIT", "Cash deposit");
    }
    // Withdraw money
    public void withdraw(String accountNumber, BigDecimal amount)
            throws InvalidAmountException, AccountNotFoundException,
            AccountFrozenException, InsufficientBalanceException {

        validateAmount(amount);

        Account account = getUsableAccount(accountNumber);

        if (account.getBalance().compareTo(amount) < 0) {

            throw new InsufficientBalanceException(
                    "Insufficient balance."
            );
        }
        account.setBalance(account.getBalance().subtract(amount));
        addTransaction(account.getAccountId(), 0, amount, "WITHDRAW", "Cash withdrawal");
    }

    public synchronized void transfer(String senderNumber, String receiverNumber,
                                       BigDecimal amount)
            throws InvalidAmountException, AccountNotFoundException,
            AccountFrozenException, InsufficientBalanceException, InvalidTransferException {
        validateAmount(amount);
        if (isBlank(senderNumber) || isBlank(receiverNumber)
                || senderNumber.trim().equalsIgnoreCase(receiverNumber.trim())) {
            throw new InvalidTransferException("Sender and receiver must be different valid accounts.");
        }
        Account sender = getUsableAccount(senderNumber);
        Account receiver = getUsableAccount(receiverNumber);
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for transfer.");
        }
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));
        addTransaction(sender.getAccountId(), receiver.getAccountId(), amount,
                "TRANSFER", "Transfer to " + receiver.getAccountNumber());
    }

    public Beneficiary addBeneficiary(int userId, String name, String accountNumber,
                                      String bankName) throws AccountNotFoundException {
        if (userService.findUserById(userId) == null || isBlank(name) || isBlank(bankName)) {
            throw new IllegalArgumentException("Beneficiary details are required.");
        }
        if (findAccountByNumber(accountNumber) == null) {
            throw new AccountNotFoundException("Beneficiary account was not found.");
        }
        for (Beneficiary beneficiary : beneficiaries) {
            if (beneficiary.getUserId() == userId
                    && beneficiary.getAccountNumber().equalsIgnoreCase(accountNumber.trim())) {
                throw new IllegalArgumentException("Beneficiary already exists.");
            }
        }
        Beneficiary beneficiary = new Beneficiary(nextBeneficiaryId++, userId, name.trim(),
                accountNumber.trim(), bankName.trim(), LocalDateTime.now());
        beneficiaries.add(beneficiary);
        return beneficiary;
    }

    public List<Beneficiary> getBeneficiariesForUser(int userId) {
        List<Beneficiary> result = new ArrayList<>();
        for (Beneficiary beneficiary : beneficiaries) {
            if (beneficiary.getUserId() == userId) {
                result.add(beneficiary);
            }
        }
        return result;
    }

    public void removeBeneficiary(int userId, int beneficiaryId) {
        beneficiaries.removeIf(item -> item.getUserId() == userId
                && item.getBeneficiaryId() == beneficiaryId);
    }

    public List<Transaction> getTransactionsForAccount(String accountNumber)
            throws AccountNotFoundException {
        Account account = getAccount(accountNumber);
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getFromAccountId() == account.getAccountId()
                    || transaction.getToAccountId() == account.getAccountId()) {
                result.add(transaction);
            }
        }
        return result;
    }

    public void freezeAccount(String accountNumber) throws AccountNotFoundException {
        getAccount(accountNumber).setStatus("FROZEN");
    }

    public void unfreezeAccount(String accountNumber) throws AccountNotFoundException {
        getAccount(accountNumber).setStatus("ACTIVE");
    }
    // Validate transaction amount
    private void validateAmount(BigDecimal amount)
            throws InvalidAmountException {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidAmountException(
                    "Amount must be greater than zero."
            );
        }
    }
    public List<Account> getAllAccounts() {
        return Collections.unmodifiableList(accounts);
    }
    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    private Account getUsableAccount(String accountNumber)
            throws AccountNotFoundException, AccountFrozenException {
        Account account = getAccount(accountNumber);
        if (!account.isActive()) {
            throw new AccountFrozenException("Account " + account.getAccountNumber() + " is frozen.");
        }
        return account;
    }

    private Account getAccount(String accountNumber) throws AccountNotFoundException {
        Account account = findAccountByNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account was not found.");
        }
        return account;
    }

    private void addTransaction(int fromAccountId, int toAccountId, BigDecimal amount,
                                String type, String description) {
        transactions.add(new Transaction(nextTransactionId++, fromAccountId, toAccountId,
                amount, type, LocalDateTime.now(), description));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}