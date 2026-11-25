package com.samdev.ledger.dto;
import com.samdev.ledger.model.Transaction;
import com.samdev.ledger.model.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(Long id, Long accountId, TransactionType type, BigDecimal amount, BigDecimal balanceAfter, String description, Long relatedAccountId, Instant createdAt) 
{

    public static TransactionResponse from(Transaction t) 
    {
        return new TransactionResponse(t.getId(), t.getAccount().getId(), t.getType(), t.getAmount(), t.getBalanceAfter(), t.getDescription(), t.getRelatedAccountId(), t.getCreatedAt());
    }
}
