package com.banking.dao;

import com.banking.model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserDAO {
    void createUser(User user) throws SQLException;
    User findByEmail(String email) throws SQLException;
    User findById(int userId) throws SQLException;
    List<User> findAll() throws SQLException;
}