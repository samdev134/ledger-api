package com.samdev.ledger.service;

import com.samdev.ledger.dto.CreateAccountRequest;
import com.samdev.ledger.exception.AccountNotFoundException;
import com.samdev.ledger.model.Account;
import com.samdev.ledger.model.Transaction;
import com.samdev.ledger.model.TransactionType;
import com.samdev.ledger.repository.AccountRepository;
import com.samdev.ledger.repository.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository,TransactionRepository transactionRepository) 
    {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Account create(CreateAccountRequest request) 
    {
        return accountRepository.save(new Account(request.ownerName(), request.currency()));
    }

    @Transactional(readOnly = true)
    public Account get(Long id) 
    {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Transactional
    public Account deposit(Long id, BigDecimal amount, String description) 
    {
        Account account = get(id);
        account.deposit(amount);
        transactionRepository.save(new Transaction(account, TransactionType.DEPOSIT, amount, description, null));
        return account;
    }

    @Transactional
    public Account withdraw(Long id, BigDecimal amount, String description) 
    {
        Account account = get(id);
        account.withdraw(amount);
        transactionRepository.save(new Transaction(account, TransactionType.WITHDRAWAL, amount, description, null));
        return account;
    }

    @Transactional(readOnly = true)
    public List<Transaction> history(Long accountId, int page, int size) 
    {
        get(accountId); // throws 404 if the account doesn't exist
        return transactionRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId, PageRequest.of(page, size))
                .getContent();
    }
}