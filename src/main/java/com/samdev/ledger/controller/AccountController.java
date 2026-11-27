package com.samdev.ledger.controller;

import com.samdev.ledger.dto.AccountResponse;
import com.samdev.ledger.dto.AmountRequest;
import com.samdev.ledger.dto.CreateAccountRequest;
import com.samdev.ledger.dto.MonthlySummaryResponse;
import com.samdev.ledger.dto.TransactionResponse;
import com.samdev.ledger.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController 
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService service;

    public AccountController(AccountService service) 
    {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) 
    {
        return AccountResponse.from(service.create(request));
    }

    @PostMapping("/{id}/deposit")
    public AccountResponse deposit(@PathVariable Long id, @Valid @RequestBody AmountRequest request) 
    {
        return AccountResponse.from(service.deposit(id, request.amount(), request.description()));
    }

    @PostMapping("/{id}/withdraw")
    public AccountResponse withdraw(@PathVariable Long id, @Valid @RequestBody AmountRequest request) 
    {
        return AccountResponse.from(service.withdraw(id, request.amount(), request.description()));
    }

    @GetMapping("/{id}/transactions")
    public List<TransactionResponse> history(@PathVariable Long id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) 
    {
        return service.history(id, page, Math.min(size, 100)).stream().map(TransactionResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable Long id) 
    {
        return AccountResponse.from(service.get(id));
    }
    
    @GetMapping("/{id}/summary")
    public MonthlySummaryResponse summary(@PathVariable Long id, @RequestParam int year, @RequestParam int month) 
    {
        return service.monthlySummary(id, year, month);
    }
}
