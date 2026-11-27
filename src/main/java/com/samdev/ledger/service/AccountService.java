package com.samdev.ledger.service;

import com.samdev.ledger.dto.CreateAccountRequest;
import com.samdev.ledger.dto.MonthlySummaryResponse;
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
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

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

    @Transactional(readOnly = true)
    public MonthlySummaryResponse monthlySummary(Long accountId, int year, int month) 
    {
        get(accountId); // 404 if the account doesn't exist

        YearMonth period = YearMonth.of(year, month); // throws if month isn't 1-12
        Instant from = period.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = period.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Transaction> transactions = transactionRepository.findInPeriod(accountId, from, to);

        BigDecimal totalIn = transactions.stream()
                .filter(t -> t.getType().isCredit())
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOut = transactions.stream()
                .filter(t -> !t.getType().isCredit())
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<TransactionType, BigDecimal> totalsByType = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getType,
                        () -> new EnumMap<>(TransactionType.class),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

        return new MonthlySummaryResponse(accountId, period.toString(), totalIn, totalOut,
                totalIn.subtract(totalOut), transactions.size(), totalsByType);
    }
    
}