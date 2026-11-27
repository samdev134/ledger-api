package com.samdev.ledger.service;

import com.samdev.ledger.dto.TransferRequest;
import com.samdev.ledger.dto.TransferResponse;
import com.samdev.ledger.exception.AccountNotFoundException;
import com.samdev.ledger.exception.IdempotencyConflictException;
import com.samdev.ledger.exception.InvalidTransferException;
import com.samdev.ledger.model.Account;
import com.samdev.ledger.model.ProcessedTransfer;
import com.samdev.ledger.model.Transaction;
import com.samdev.ledger.model.TransactionType;
import com.samdev.ledger.repository.AccountRepository;
import com.samdev.ledger.repository.ProcessedTransferRepository;
import com.samdev.ledger.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ProcessedTransferRepository processedTransferRepository;

    public TransferService(AccountRepository accountRepository, TransactionRepository transactionRepository, ProcessedTransferRepository processedTransferRepository) 
    {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.processedTransferRepository = processedTransferRepository;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request, String idempotencyKey) 
    {
        // 1. Have we already processed this exact request?
        if (idempotencyKey != null) 
            {
            if (idempotencyKey.isBlank() || idempotencyKey.length() > 100) 
                {
                    throw new InvalidTransferException("Idempotency-Key must be 1-100 characters");
                }
            Optional<ProcessedTransfer> existing = processedTransferRepository.findById(idempotencyKey);
            if (existing.isPresent()) 
                {
                ProcessedTransfer previous = existing.get();
                if (!previous.matches(request.fromAccountId(), request.toAccountId(), request.amount())) 
                    {
                    throw new IdempotencyConflictException(idempotencyKey);
                    }
                return new TransferResponse(previous.getFromAccountId(), previous.getToAccountId(),
                        previous.getAmount(), previous.getFromBalanceAfter(), previous.getToBalanceAfter());
                }
            }

        // 2. Validate
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

        // 3. Move the money
        from.withdraw(request.amount());
        to.deposit(request.amount());

        // 4. Record it in the ledger
        transactionRepository.save(new Transaction(from, TransactionType.TRANSFER_OUT, request.amount(), request.description(), to.getId()));
        transactionRepository.save(new Transaction(to, TransactionType.TRANSFER_IN, request.amount(), request.description(), from.getId()));

        // 5. Remember the key so a retry returns this result instead of transferring again
        if (idempotencyKey != null) 
        {
            processedTransferRepository.save(new ProcessedTransfer(idempotencyKey,from.getId(), to.getId(), request.amount(), from.getBalance(), to.getBalance()));
        }

        return new TransferResponse(from.getId(), to.getId(), request.amount(), from.getBalance(), to.getBalance());
    }
}