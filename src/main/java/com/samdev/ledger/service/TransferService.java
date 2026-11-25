package com.samdev.ledger.service;

import com.samdev.ledger.dto.TransferRequest;
import com.samdev.ledger.dto.TransferResponse;
import com.samdev.ledger.exception.AccountNotFoundException;
import com.samdev.ledger.exception.InvalidTransferException;
import com.samdev.ledger.model.Account;
import com.samdev.ledger.model.Transaction;
import com.samdev.ledger.model.TransactionType;
import com.samdev.ledger.repository.AccountRepository;
import com.samdev.ledger.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service 
public class TransferService 
{
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(AccountRepository accountRepository,TransactionRepository transactionRepository) 
    {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) 
    {
        if (request.fromAccountId().equals(request.toAccountId())) 
        {
            throw new InvalidTransferException("Cannot transfer to the same account");
        }

        Account from = accountRepository.findById(request.fromAccountId()).orElseThrow(() -> new AccountNotFoundException(request.fromAccountId()));
        Account to = accountRepository.findById(request.toAccountId()).orElseThrow(() -> new AccountNotFoundException(request.toAccountId()));

        if (!from.getCurrency().equals(to.getCurrency())) 
        {
            throw new InvalidTransferException("Currency mismatch: " + from.getCurrency() + " -> " + to.getCurrency());
        }
    
        from.withdraw(request.amount()); // throws if insufficient funds -> whole transfer rolls back
        to.deposit(request.amount());

        transactionRepository.save(new Transaction(from, TransactionType.TRANSFER_OUT, request.amount(), request.description(), to.getId()));
        transactionRepository.save(new Transaction(to, TransactionType.TRANSFER_IN, request.amount(), request.description(), from.getId()));

        return new TransferResponse(from.getId(), to.getId(), request.amount(), from.getBalance(), to.getBalance());
    }
}
