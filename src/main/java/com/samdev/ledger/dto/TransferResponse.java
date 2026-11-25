package com.samdev.ledger.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TransferResponse 
(
    Long fromAccountId, Long toAccountId, BigDecimal amount, BigDecimal fromBalanceAfter, BigDecimal toBalanceAfter
)   {}
