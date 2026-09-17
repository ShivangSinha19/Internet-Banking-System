package com.banking;

import com.banking.exception.AccountFrozenException;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.InsufficientBalanceException;
import com.banking.exception.InvalidAmountException;
import com.banking.exception.InvalidTransferException;
import com.banking.model.Account;
import com.banking.model.Beneficiary;
import com.banking.model.Transaction;
import com.banking.model.User;
import com.banking.service.BankingService;
import com.banking.service.UserService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final UserService userService = new UserService();
    private final BankingService bankingService = new BankingService(userService);

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        seedAdmin();
        boolean running = true;
        while (running) {
            System.out.println("\n====== INTERNET BANKING SYSTEM ======");
            System.out.println("1. Register\n2. Login\n3. Exit");
            switch (readLine("Choose an option: ")) {
                case "1": register(); break;
                case "2": login(); break;
                case "3": running = false; break;
                default: System.out.println("Invalid option.");
            }
        }
        System.out.println("Goodbye.");
    }

    private void seedAdmin() {
        userService.registerAdmin("System", "Administrator", "admin@bank.com", "admin123");
        System.out.println("Demo admin login: admin@bank.com / admin123");
    }

    private void register() {
        try {
            boolean registered = userService.registerUser(readLine("First name: "),
                    readLine("Last name: "), readLine("Email: "), readLine("Password: "));
            System.out.println(registered ? "Registration successful." : "Email is already registered.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Registration failed: " + exception.getMessage());
        }
    }

    private void login() {
        User user = userService.login(readLine("Email: "), readLine("Password: "));
        if (user == null) {
            System.out.println("Invalid email or password.");
        } else if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            adminMenu(user);
        } else {
            customerMenu(user);
        }
    }

    private void customerMenu(User user) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n====== CUSTOMER DASHBOARD ======");
            System.out.println("1. View Profile\n2. Create Account\n3. View My Accounts");
            System.out.println("4. Check Balance\n5. Deposit\n6. Withdraw\n7. Transfer Money");
            System.out.println("8. Transaction History\n9. Add Beneficiary\n10. View Beneficiaries");
            System.out.println("11. Remove Beneficiary\n12. Logout");
            switch (readLine("Choose an option: ")) {
                case "1": System.out.println(user); break;
                case "2": createAccount(user); break;
                case "3": printAccounts(bankingService.getAccountsForUser(user.getUserId())); break;
                case "4": checkBalance(user); break;
                case "5": deposit(user); break;
                case "6": withdraw(user); break;
                case "7": transfer(user); break;
                case "8": history(user); break;
                case "9": addBeneficiary(user); break;
                case "10": printBeneficiaries(user); break;
                case "11": removeBeneficiary(user); break;
                case "12": loggedIn = false; break;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private void createAccount(User user) {
        try {
            Account account = bankingService.createAccount(user.getUserId(),
                    readLine("Account number: "), "SAVINGS");
            System.out.println("Created: " + account);
        } catch (IllegalArgumentException exception) {
            System.out.println("Could not create account: " + exception.getMessage());
        }
    }

    private void checkBalance(User user) {
        Account account = findOwnedAccount(user);
        if (account != null) {
            System.out.println("Balance: " + account.getBalance() + " (" + account.getStatus() + ")");
        }
    }

    private void deposit(User user) {
        Account account = findOwnedAccount(user);
        if (account == null) return;
        try {
            bankingService.deposit(account.getAccountNumber(), readAmount());
            System.out.println("Deposit successful. Balance: " + account.getBalance());
        } catch (Exception exception) { printFailure(exception); }
    }

    private void withdraw(User user) {
        Account account = findOwnedAccount(user);
        if (account == null) return;
        try {
            bankingService.withdraw(account.getAccountNumber(), readAmount());
            System.out.println("Withdrawal successful. Balance: " + account.getBalance());
        } catch (Exception exception) { printFailure(exception); }
    }

    private void transfer(User user) {
        Account sender = findOwnedAccount(user);
        if (sender == null) return;
        try {
            bankingService.transfer(sender.getAccountNumber(), readLine("Receiver account: "), readAmount());
            System.out.println("Transfer successful. Balance: " + sender.getBalance());
        } catch (Exception exception) { printFailure(exception); }
    }

    private void history(User user) {
        Account account = findOwnedAccount(user);
        if (account == null) return;
        try {
            for (Transaction transaction : bankingService.getTransactionsForAccount(account.getAccountNumber())) {
                System.out.println(transaction);
            }
        } catch (AccountNotFoundException exception) { printFailure(exception); }
    }

    private void addBeneficiary(User user) {
        try {
            Beneficiary beneficiary = bankingService.addBeneficiary(user.getUserId(),
                    readLine("Name: "), readLine("Account number: "), readLine("Bank name: "));
            System.out.println("Added: " + beneficiary);
        } catch (Exception exception) { printFailure(exception); }
    }

    private void printBeneficiaries(User user) {
        for (Beneficiary beneficiary : bankingService.getBeneficiariesForUser(user.getUserId())) {
            System.out.println(beneficiary);
        }
    }

    private void removeBeneficiary(User user) {
        printBeneficiaries(user);
        bankingService.removeBeneficiary(user.getUserId(), readInt("Beneficiary ID: "));
        System.out.println("Beneficiary removed if it belonged to your profile.");
    }

    private void adminMenu(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n====== ADMIN DASHBOARD ======");
            System.out.println("1. View All Users\n2. View All Accounts\n3. Search User");
            System.out.println("4. Search Account\n5. View All Transactions\n6. Freeze Account");
            System.out.println("7. Unfreeze Account\n8. Banking Statistics\n9. Logout");
            switch (readLine("Choose an option: ")) {
                case "1": userService.getAllUsers().forEach(System.out::println); break;
                case "2": bankingService.getAllAccounts().forEach(System.out::println); break;
                case "3": searchUser(); break;
                case "4": searchAccount(); break;
                case "5": bankingService.getAllTransactions().forEach(System.out::println); break;
                case "6": setAccountState(true); break;
                case "7": setAccountState(false); break;
                case "8": printStatistics(); break;
                case "9": loggedIn = false; break;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private void searchUser() {
        User user = userService.findUserByEmail(readLine("Email: "));
        System.out.println(user == null ? "User not found." : user);
    }

    private void searchAccount() {
        Account account = bankingService.findAccountByNumber(readLine("Account number: "));
        System.out.println(account == null ? "Account not found." : account);
    }

    private void setAccountState(boolean freeze) {
        try {
            if (freeze) bankingService.freezeAccount(readLine("Account number: "));
            else bankingService.unfreezeAccount(readLine("Account number: "));
            System.out.println(freeze ? "Account frozen." : "Account unfrozen.");
        } catch (AccountNotFoundException exception) { printFailure(exception); }
    }

    private void printStatistics() {
        System.out.println("Users: " + userService.getAllUsers().size());
        System.out.println("Accounts: " + bankingService.getAllAccounts().size());
        System.out.println("Transactions: " + bankingService.getAllTransactions().size());
    }

    private Account findOwnedAccount(User user) {
        List<Account> accounts = bankingService.getAccountsForUser(user.getUserId());
        if (accounts.isEmpty()) {
            System.out.println("No accounts found. Create an account first.");
            return null;
        }
        printAccounts(accounts);
        String number = readLine("Account number: ");
        for (Account account : accounts) {
            if (account.getAccountNumber().equalsIgnoreCase(number)) return account;
        }
        System.out.println("That account does not belong to you.");
        return null;
    }

    private void printAccounts(List<Account> accounts) {
        if (accounts.isEmpty()) System.out.println("No accounts found.");
        accounts.forEach(System.out::println);
    }

    private BigDecimal readAmount() {
        return new BigDecimal(readLine("Amount: "));
    }

    private int readInt(String prompt) {
        return Integer.parseInt(readLine(prompt));
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private void printFailure(Exception exception) {
        System.out.println("Operation failed: " + exception.getMessage());
    }
}