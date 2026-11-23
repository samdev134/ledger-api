package com.samdev.ledger.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long id)
    {
        super("Account " + id + " not found");
    }
}
