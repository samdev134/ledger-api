package com.samdev.ledger.model;

public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_IN,
    TRANSFER_OUT;

    public boolean isCredit()
    {
        return this == DEPOSIT || this == TRANSFER_IN;
    }
}
