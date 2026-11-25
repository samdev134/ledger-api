package com.samdev.ledger.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AmountRequest (
    @NotNull
    @DecimalMin(value = "0.01", message = "must be at least 0.01")
    @Digits(integer = 17, fraction = 2, message = "max 2 decimal places")
    BigDecimal amount,

    @Size(max = 255)
    String description
) {}
    

