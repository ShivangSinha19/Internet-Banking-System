package com.banking.repository;

import com.banking.entity.BeneficiaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiaryRepository extends JpaRepository<BeneficiaryEntity, Integer> {
    List<BeneficiaryEntity> findByUser_UserId(Integer userId);
    void deleteByUser_UserIdAndBeneficiaryId(Integer userId, Integer beneficiaryId);
    boolean existsByUser_UserIdAndAccountNumberIgnoreCase(Integer userId, String accountNumber);
}