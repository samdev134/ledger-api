package com.samdev.ledger;

import com.samdev.ledger.dto.CreateAccountRequest;
import com.samdev.ledger.model.Account;
import com.samdev.ledger.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class TransferIntegrationTest {

    @Autowired WebApplicationContext context;
    @Autowired AccountService accountService;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() 
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private Long newAccount(String currency, String startingBalance) 
    {
        Account account = accountService.create(new CreateAccountRequest("Test", currency));
        BigDecimal amount = new BigDecimal(startingBalance);
        if (amount.signum() > 0) 
        {
            accountService.deposit(account.getId(), amount, null);
        }
        return account.getId();
    }

    private String transferJson(Long from, Long to, String amount) 
    {
        return """
               {"fromAccountId": %d, "toAccountId": %d, "amount": %s}
               """.formatted(from, to, amount);
    }

    @Test
    void transferMovesMoneyBetweenAccounts() throws Exception 
    {
        Long from = newAccount("GBP", "100");
        Long to = newAccount("GBP", "0");

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(from, to, "40")))
                .andExpect(status().isOk());

        assertThat(accountService.get(from).getBalance()).isEqualByComparingTo("60");
        assertThat(accountService.get(to).getBalance()).isEqualByComparingTo("40");
    }

    @Test
    void failedTransferRollsBackCompletely() throws Exception 
    {
        Long from = newAccount("GBP", "30");
        Long to = newAccount("GBP", "0");

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(from, to, "50")))
                .andExpect(status().isConflict());

        assertThat(accountService.get(from).getBalance()).isEqualByComparingTo("30");
        assertThat(accountService.get(to).getBalance()).isEqualByComparingTo("0");
        assertThat(accountService.history(from, 0, 20)).hasSize(1); // only the opening deposit
    }

    @Test
    void sameIdempotencyKeyOnlyTransfersOnce() throws Exception 
    {
        Long from = newAccount("GBP", "100");
        Long to = newAccount("GBP", "0");

        for (int i = 0; i < 2; i++) 
        {
            mockMvc.perform(post("/api/transfers")
                            .header("Idempotency-Key", "integration-key-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(transferJson(from, to, "40")))
                    .andExpect(status().isOk());
        }

        assertThat(accountService.get(from).getBalance()).isEqualByComparingTo("60"); // not 20
    }

    @Test
    void reusingKeyForDifferentTransferIsRejected() throws Exception 
    {
        Long from = newAccount("GBP", "100");
        Long to = newAccount("GBP", "0");

        mockMvc.perform(post("/api/transfers")
                        .header("Idempotency-Key", "integration-key-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(from, to, "40")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/transfers")
                        .header("Idempotency-Key", "integration-key-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(from, to, "10")))
                .andExpect(status().isConflict());
    }

    @Test
    void negativeAmountIsRejected() throws Exception 
    {
        Long from = newAccount("GBP", "100");
        Long to = newAccount("GBP", "0");

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(from, to, "-5")))
                .andExpect(status().isBadRequest());
    }
}