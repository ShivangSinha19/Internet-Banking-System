package com.banking.repository;

import com.banking.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {
    Optional<AccountEntity> findByAccountNumberIgnoreCase(String accountNumber);
    List<AccountEntity> findByUser_UserId(Integer userId);
    Optional<AccountEntity> findByUser_UserIdAndAccountNumberIgnoreCase(Integer userId, String accountNumber);
    boolean existsByAccountNumberIgnoreCase(String accountNumber);
}