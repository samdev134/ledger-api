package com.samdev.ledger.repository;

import com.samdev.ledger.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
}
