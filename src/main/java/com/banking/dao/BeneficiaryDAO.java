package com.banking.dao;

import com.banking.model.Beneficiary;

import java.sql.SQLException;
import java.util.List;

public interface BeneficiaryDAO {
    void createBeneficiary(Beneficiary beneficiary) throws SQLException;
    List<Beneficiary> findByUserId(int userId) throws SQLException;
    void deleteBeneficiary(int userId, int beneficiaryId) throws SQLException;
}