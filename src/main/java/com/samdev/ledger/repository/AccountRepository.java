package com.samdev.ledger.repository;

import com.samdev.ledger.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends  JpaRepository<Account, Long> {
}
