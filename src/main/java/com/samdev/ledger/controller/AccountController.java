package com.samdev.ledger.controller;

import com.samdev.ledger.dto.AccountResponse;
import com.samdev.ledger.dto.AmountRequest;
import com.samdev.ledger.dto.CreateAccountRequest;
import com.samdev.ledger.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable Long id) 
    {
        return AccountResponse.from(service.get(id));
    }

    @PostMapping("/{id}/deposit")
    public AccountResponse deposit(@PathVariable Long id, @Valid @RequestBody AmountRequest request) 
    {
        return AccountResponse.from(service.deposit(id, request.amount()));
    }

    @PostMapping("/{id}/withdraw")
    public AccountResponse withdraw(@PathVariable Long id, @Valid @RequestBody AmountRequest request) 
    {
        return AccountResponse.from(service.withdraw(id, request.amount()));
    }

    
}
