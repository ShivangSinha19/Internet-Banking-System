package com.banking.service;

import com.banking.exception.AccountFrozenException;
import com.banking.exception.InsufficientBalanceException;
import com.banking.exception.InvalidTransferException;
import com.banking.model.Account;
import com.banking.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankingServiceTest {
    private UserService userService;
    private BankingService bankingService;
    private User user;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        bankingService = new BankingService(userService);
        userService.registerUser("Asha", "Kumar", "asha@example.com", "asha123");
        user = userService.login("asha@example.com", "asha123");
    }

    @Test
    void registersUser() {
        UserService service = new UserService();

        assertTrue(service.registerUser("Ben", "User", "ben@example.com", "ben123"));
        assertNotNull(service.findUserByEmail("ben@example.com"));
    }

    @Test
    void rejectsDuplicateRegistration() {
        assertTrue(!userService.registerUser("Other", "Name", "asha@example.com", "other123"));
    }

    @Test
    void logsInSuccessfully() {
        assertEquals(user, userService.login("ASHA@example.com", "asha123"));
    }

    @Test
    void rejectsFailedLogin() {
        assertNull(userService.login("asha@example.com", "wrong-password"));
    }

    @Test
    void createsAccountForExistingUser() {
        Account account = bankingService.createAccount(user.getUserId(), "AC001", "SAVINGS");

        assertEquals("AC001", account.getAccountNumber());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals(1, bankingService.getAccountsForUser(user.getUserId()).size());
    }

    @Test
    void depositsMoney() throws Exception {
        Account account = createAccount("AC001");

        bankingService.deposit(account.getAccountNumber(), new BigDecimal("100.00"));

        assertEquals(new BigDecimal("100.00"), account.getBalance());
    }

    @Test
    void withdrawsMoney() throws Exception {
        Account account = createAccount("AC001");
        bankingService.deposit(account.getAccountNumber(), new BigDecimal("100.00"));

        bankingService.withdraw(account.getAccountNumber(), new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), account.getBalance());
    }

    @Test
    void rejectsInsufficientBalance() throws Exception {
        Account account = createAccount("AC001");

        assertThrows(InsufficientBalanceException.class,
                () -> bankingService.withdraw(account.getAccountNumber(), new BigDecimal("1.00")));
    }

    @Test
    void transfersAcrossAccounts() throws Exception {
        Account sender = createAccount("AC001");
        Account receiver = createAccount("AC002");
        bankingService.deposit(sender.getAccountNumber(), new BigDecimal("100.00"));

        bankingService.transfer(sender.getAccountNumber(), receiver.getAccountNumber(),
                new BigDecimal("35.00"));

        assertEquals(new BigDecimal("65.00"), sender.getBalance());
        assertEquals(new BigDecimal("35.00"), receiver.getBalance());
    }

    @Test
    void rejectsSameAccountTransfer() throws Exception {
        Account account = createAccount("AC001");

        assertThrows(InvalidTransferException.class,
                () -> bankingService.transfer(account.getAccountNumber(), account.getAccountNumber(),
                        new BigDecimal("10.00")));
    }

    @Test
    void rejectsOperationOnFrozenAccount() throws Exception {
        Account account = createAccount("AC001");
        bankingService.freezeAccount(account.getAccountNumber());

        assertThrows(AccountFrozenException.class,
                () -> bankingService.deposit(account.getAccountNumber(), new BigDecimal("10.00")));
    }

    private Account createAccount(String accountNumber) {
        return bankingService.createAccount(user.getUserId(), accountNumber, "SAVINGS");
    }
}