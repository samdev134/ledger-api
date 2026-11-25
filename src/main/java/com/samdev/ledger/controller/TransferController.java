package com.samdev.ledger.controller;

import com.samdev.ledger.dto.TransferRequest;
import com.samdev.ledger.dto.TransferResponse;
import com.samdev.ledger.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController 
{
    private final TransferService service;

    public TransferController(TransferService service)
    {
        this.service = service;
    }

    @PostMapping 
    public TransferResponse transfer(@Valid  @RequestBody TransferRequest request)
    {
        return service.transfer(request);
    }
}
