package com.samdev.ledger.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions") 
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    private String description;

    private Long relatedAccountId; // the other account in a transfer

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Transaction() {}

    public Transaction(Account account, TransactionType type, BigDecimal amount, String description, Long relatedAccountId) 
    {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = account.getBalance();
        this.description = description;
        this.relatedAccountId = relatedAccountId;
    }

    public Long getId() { return id; }
    public Account getAccount() { return account; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public Long getRelatedAccountId() { return relatedAccountId; }
    public Instant getCreatedAt() { return createdAt; }
}
