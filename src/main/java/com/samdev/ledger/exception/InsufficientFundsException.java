package com.samdev.ledger.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(Long id, BigDecimal balence, BigDecimal requested)
    {
        super("Account " + id + " has insufficient funds: balence " + balence + ", requested " + requested);
    }
}
