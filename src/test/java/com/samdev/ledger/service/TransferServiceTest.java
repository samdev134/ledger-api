package com.samdev.ledger.service;

import com.samdev.ledger.dto.TransferRequest;
import com.samdev.ledger.dto.TransferResponse;
import com.samdev.ledger.exception.InsufficientFundsException;
import com.samdev.ledger.exception.InvalidTransferException;
import com.samdev.ledger.model.Account;
import com.samdev.ledger.model.ProcessedTransfer;
import com.samdev.ledger.model.Transaction;
import com.samdev.ledger.repository.AccountRepository;
import com.samdev.ledger.repository.ProcessedTransferRepository;
import com.samdev.ledger.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock ProcessedTransferRepository processedTransferRepository;

    @InjectMocks TransferService transferService;

    private Account accountWithBalance(String currency, String balance) 
    {
        Account account = new Account("Test", currency);
        account.deposit(new BigDecimal(balance));
        return account;
    }

    @Test
    void rejectsTransferToSameAccount() 
    {
        TransferRequest request = new TransferRequest(1L, 1L, new BigDecimal("10"), null);

        assertThatThrownBy(() -> transferService.transfer(request, null)).isInstanceOf(InvalidTransferException.class);

        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @Test
    void rejectsCurrencyMismatch() 
    {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountWithBalance("GBP", "100")));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountWithBalance("EUR", "0")));
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("10"), null);

        assertThatThrownBy(() -> transferService.transfer(request, null)).isInstanceOf(InvalidTransferException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void insufficientFundsMovesNoMoneyAndRecordsNothing() 
    {
        Account from = accountWithBalance("GBP", "30");
        Account to = accountWithBalance("GBP", "0");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(from));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(to));
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("50"), null);

        assertThatThrownBy(() -> transferService.transfer(request, null)).isInstanceOf(InsufficientFundsException.class);

        assertThat(from.getBalance()).isEqualByComparingTo("30");
        assertThat(to.getBalance()).isEqualByComparingTo("0");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void successfulTransferMovesMoneyAndRecordsTwoEntries() 
    {
        Account from = accountWithBalance("GBP", "100");
        Account to = accountWithBalance("GBP", "0");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(from));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(to));
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("40"), "Pizza");

        TransferResponse response = transferService.transfer(request, null);

        assertThat(response.fromBalanceAfter()).isEqualByComparingTo("60");
        assertThat(response.toBalanceAfter()).isEqualByComparingTo("40");
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void retryWithSameKeyReturnsStoredResultWithoutMovingMoney() 
    {
        ProcessedTransfer stored = new ProcessedTransfer("abc", 1L, 2L, new BigDecimal("40"), new BigDecimal("60"), new BigDecimal("40"));
        when(processedTransferRepository.findById("abc")).thenReturn(Optional.of(stored));
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("40"), null);

        TransferResponse response = transferService.transfer(request, "abc");

        assertThat(response.fromBalanceAfter()).isEqualByComparingTo("60");
        verifyNoInteractions(accountRepository, transactionRepository);
    }
}
