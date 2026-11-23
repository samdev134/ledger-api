package com.samdev.ledger.dto;

import com.samdev.ledger.model.Account;
import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse (Long id, String ownerName, String currency, BigDecimal balance, Instant createdAt)
{
    public static AccountResponse from(Account a)
    {
        return new AccountResponse(a.getId(), a.getOwnerName(), a.getCurrency(), a.getBalance(), a.getCreatedAt());
    }
}
    

