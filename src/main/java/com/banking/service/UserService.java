package com.banking.service;

import com.banking.dao.UserDAO;
import com.banking.model.User;
import com.banking.util.CredentialHasher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.sql.SQLException;

public class UserService {

        private final List<User> users = new ArrayList<>();
        private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
        private final UserDAO userDAO;

        public UserService() {
            this(null);
        }

        public UserService(UserDAO userDAO) {
            this.userDAO = userDAO;
        }

    private int nextUserId = 1;

    // Register a new customer
    public boolean registerUser(String firstName,
                                String lastName,
                                String email,
                                String password) {

        validateUserDetails(firstName, lastName, email, password);
        if (findUserByEmail(email) != null) {
            return false;
        }

        String storedPassword = userDAO == null ? password : CredentialHasher.hash(password);
        User user = new User(
                nextUserId,
                firstName,
                lastName,
                email,
                storedPassword,
                "CUSTOMER"
        );

        saveUser(user);
        nextUserId++;

        return true;
    }

    public User registerAdmin(String firstName, String lastName,
                              String email, String password) {
        validateUserDetails(firstName, lastName, email, password);
        if (findUserByEmail(email) != null) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        String storedPassword = userDAO == null ? password : CredentialHasher.hash(password);
        User admin = new User(nextUserId++, firstName.trim(), lastName.trim(),
            email.trim().toLowerCase(), storedPassword, "ADMIN");
        saveUser(admin);
        return admin;
    }

    // Login
    public User login(String email, String password) {

        User user = findUserByEmail(email);

        boolean passwordMatches = user != null && (userDAO == null
            ? user.getPassword().equals(password)
            : CredentialHasher.matches(password, user.getPassword()));
        if (passwordMatches) {

            return user;
        }

        return null;
    }

    // Find user by email
    public User findUserByEmail(String email) {

        if (userDAO != null) {
            try {
                return userDAO.findByEmail(email);
            } catch (SQLException exception) {
                throw new IllegalStateException("Could not read user from the database.", exception);
            }
        }

        for (User user : users) {

            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }

        return null;
    }

    // Get all users
    public List<User> getAllUsers() {
        if (userDAO != null) {
            try {
                return Collections.unmodifiableList(userDAO.findAll());
            } catch (SQLException exception) {
                throw new IllegalStateException("Could not read users from the database.", exception);
            }
        }
        return Collections.unmodifiableList(users);
    }

    public User findUserById(int userId) {
        if (userDAO != null) {
            try {
                return userDAO.findById(userId);
            } catch (SQLException exception) {
                throw new IllegalStateException("Could not read user from the database.", exception);
            }
        }
        for (User user : users) {
            if (user.getUserId() == userId) {
                return user;
            }
        }
        return null;
    }

    private void validateUserDetails(String firstName, String lastName,
                                     String email, String password) {
        if (isBlank(firstName) || isBlank(lastName)) {
            throw new IllegalArgumentException("First and last names are required.");
        }
        if (isBlank(email) || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("A valid email address is required.");
        }
        if (password == null || password.length() < 6 || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password must contain at least 6 characters.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void saveUser(User user) {
        if (userDAO == null) {
            users.add(user);
            return;
        }
        try {
            userDAO.createUser(user);
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not save user to the database.", exception);
        }
    }
}