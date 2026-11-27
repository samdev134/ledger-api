package com.samdev.ledger.dto;

import com.samdev.ledger.model.TransactionType;
import java.math.BigDecimal;
import java.util.Map;

public record MonthlySummaryResponse
(
    Long accountID, String month, BigDecimal totalIn, BigDecimal totalOut, BigDecimal net, int transactionCount, Map<TransactionType, BigDecimal> totalsByType
)
{}
