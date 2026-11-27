package com.samdev.ledger.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "processed_transfers")
public class ProcessedTransfer {

    @Id
    @Column(length = 100)
    private String idempotencyKey;

    @Column(nullable = false)
    private Long fromAccountId;

    @Column(nullable = false)
    private Long toAccountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fromBalanceAfter;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal toBalanceAfter;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ProcessedTransfer() {}

    public ProcessedTransfer(String idempotencyKey, Long fromAccountId, Long toAccountId, BigDecimal amount, BigDecimal fromBalanceAfter, BigDecimal toBalanceAfter) 
    {
        this.idempotencyKey = idempotencyKey;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.fromBalanceAfter = fromBalanceAfter;
        this.toBalanceAfter = toBalanceAfter;
    }

    /** True if a retried request is the same transfer as the one stored under this key. */
    public boolean matches(Long fromAccountId, Long toAccountId, BigDecimal amount) 
    {
        return this.fromAccountId.equals(fromAccountId) && this.toAccountId.equals(toAccountId) && this.amount.compareTo(amount) == 0;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public Long getFromAccountId() { return fromAccountId; }
    public Long getToAccountId() { return toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getFromBalanceAfter() { return fromBalanceAfter; }
    public BigDecimal getToBalanceAfter() { return toBalanceAfter; }
    
}
