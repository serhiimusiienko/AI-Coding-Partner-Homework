package com.example.banking.web;

import com.example.banking.dto.AccountSummaryResponse;
import com.example.banking.dto.BalanceResponse;
import com.example.banking.service.TransactionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final TransactionService service;

    public AccountController(TransactionService service) {
        this.service = service;
    }

    @GetMapping("/{accountId}/balance")
    public BalanceResponse balance(@PathVariable String accountId) {
        BigDecimal bal = service.getBalance(accountId);
        return new BalanceResponse(accountId, bal);
    }

    @GetMapping("/{accountId}/summary")
    public AccountSummaryResponse summary(@PathVariable String accountId) {
        return service.getSummary(accountId);
    }
}
