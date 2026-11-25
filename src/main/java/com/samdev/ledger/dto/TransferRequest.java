package com.samdev.ledger.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TransferRequest
(
    @NotNull Long fromAccountId,
    @NotNull Long toAccountId,

    @NotNull
    @DecimalMin(value = "0.01", message = "must be at least 0.01")
    @Digits(integer = 17, fraction = 2, message = "max 2 decimal places")
    BigDecimal amount,

    @Size(max = 255)
    String description
)   {}

