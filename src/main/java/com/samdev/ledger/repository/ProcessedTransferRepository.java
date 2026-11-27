package com.samdev.ledger.repository;

import com.samdev.ledger.model.ProcessedTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedTransferRepository extends JpaRepository<ProcessedTransfer, String> {
}
