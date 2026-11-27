package com.samdev.ledger.repository;

import com.samdev.ledger.model.Transaction;

import jakarta.persistence.criteria.CriteriaBuilder.In;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    @Query("""
            select t from Transaction t
            where t.account.id = :accountId
            and t.createdAt >= :from
            and t.createdAt < :to
            """)

    List<Transaction> findInPeriod(@Param("accountId") Long accountId, @Param("from") Instant from, @Param("to") Instant to);
}
