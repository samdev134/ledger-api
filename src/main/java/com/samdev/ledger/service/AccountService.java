package com.samdev.ledger.service;

import com.samdev.ledger.dto.CreateAccountRequest;
import com.samdev.ledger.exception.AccountNotFoundException;
import com.samdev.ledger.model.Account;
import com.samdev.ledger.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service 
public class AccountService {
    private final AccountRepository repository;

    public AccountService(AccountRepository repository)
    {
        this.repository = repository;
    }

    @Transactional
    public Account create(CreateAccountRequest request) 
    {
        return repository.save(new Account(request.ownerName(), request.currency()));
    }

    @Transactional(readOnly = true)
    public Account get(Long id) 
    {
        return repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Transactional
    public Account deposit(Long id, BigDecimal amount) 
    {
        Account account = get(id);
        account.deposit(amount);
        return account; // no save() needed: JPA saves changes when the transaction commits
    }

    @Transactional
    public Account withdraw(Long id, BigDecimal amount) 
    {
        Account account = get(id);
        account.withdraw(amount);
        return account;
    }
    
}
