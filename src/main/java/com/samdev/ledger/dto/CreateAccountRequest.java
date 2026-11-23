package com.samdev.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateAccountRequest (
    @NotBlank String ownerName,
    @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "must be a 3-letter currency code like GBP")
    String currency
    
) {}
