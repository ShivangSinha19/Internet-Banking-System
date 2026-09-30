package com.banking.repository;

import com.banking.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByFromAccount_AccountIdOrToAccount_AccountIdOrderByTransactionDateDesc(
            Integer fromAccountId, Integer toAccountId);
}