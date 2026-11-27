package com.samdev.ledger.model;

import com.samdev.ledger.exception.InsufficientFundsException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    @Test
    void depositIncreasesBalance() 
    {
        Account account = new Account("Sam", "GBP");
        account.deposit(new BigDecimal("50.00"));
        assertThat(account.getBalance()).isEqualByComparingTo("50.00");
    }

    @Test
    void withdrawDecreasesBalance() 
    {
        Account account = new Account("Sam", "GBP");
        account.deposit(new BigDecimal("100"));
        account.withdraw(new BigDecimal("30"));
        assertThat(account.getBalance()).isEqualByComparingTo("70");
    }

    @Test
    void canWithdrawExactBalance() 
    {
        Account account = new Account("Sam", "GBP");
        account.deposit(new BigDecimal("25"));
        account.withdraw(new BigDecimal("25"));
        assertThat(account.getBalance()).isEqualByComparingTo("0");
    }

    @Test
    void withdrawingTooMuchThrowsAndLeavesBalanceUnchanged() 
    {
        Account account = new Account("Sam", "GBP");
        account.deposit(new BigDecimal("20"));

        assertThatThrownBy(() -> account.withdraw(new BigDecimal("50"))).isInstanceOf(InsufficientFundsException.class);

        assertThat(account.getBalance()).isEqualByComparingTo("20");
    }
}
