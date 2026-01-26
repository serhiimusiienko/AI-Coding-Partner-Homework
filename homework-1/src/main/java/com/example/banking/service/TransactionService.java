package com.example.banking.service;

import com.example.banking.dto.AccountSummaryResponse;
import com.example.banking.dto.CreateTransactionRequest;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.model.Transaction;
import com.example.banking.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository repository = new TransactionRepository();

    public TransactionResponse createTransaction(CreateTransactionRequest req) {
        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setFromAccount(req.getFromAccount());
        tx.setToAccount(req.getToAccount());
        tx.setAmount(req.getAmount());
        tx.setCurrency(req.getCurrency());
        tx.setType(req.getType());
        tx.setTimestamp(Instant.now());
        tx.setStatus("completed");
        repository.save(tx);
        return toResponse(tx);
    }

    public TransactionResponse getById(String id) {
        Transaction tx = repository.findById(id).orElseThrow(NoSuchElementException::new);
        return toResponse(tx);
    }

    public List<TransactionResponse> list(String accountId, String type, Instant from, Instant to) {
        return repository.findByFilters(accountId, type, from, to).stream()
                .sorted(Comparator.comparing(Transaction::getTimestamp))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BigDecimal getBalance(String accountId) {
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction tx : repository.findAll()) {
            if ("deposit".equals(tx.getType()) && accountId.equals(tx.getToAccount())) {
                balance = balance.add(tx.getAmount());
            } else if ("withdrawal".equals(tx.getType()) && accountId.equals(tx.getFromAccount())) {
                balance = balance.subtract(tx.getAmount());
            } else if ("transfer".equals(tx.getType())) {
                if (accountId.equals(tx.getFromAccount())) {
                    balance = balance.subtract(tx.getAmount());
                }
                if (accountId.equals(tx.getToAccount())) {
                    balance = balance.add(tx.getAmount());
                }
            }
        }
        return balance;
    }

    public AccountSummaryResponse getSummary(String accountId) {
        List<Transaction> transactions = repository.findAll().stream()
                .filter(tx -> accountId.equals(tx.getFromAccount()) || accountId.equals(tx.getToAccount()))
                .collect(Collectors.toList());
        BigDecimal deposits = transactions.stream()
                .filter(tx -> "deposit".equals(tx.getType()) && accountId.equals(tx.getToAccount()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal withdrawals = transactions.stream()
                .filter(tx -> "withdrawal".equals(tx.getType()) && accountId.equals(tx.getFromAccount()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Instant mostRecent = transactions.stream()
                .map(Transaction::getTimestamp)
                .max(Instant::compareTo)
                .orElse(null);
        AccountSummaryResponse res = new AccountSummaryResponse();
        res.setAccountId(accountId);
        res.setTotalDeposits(deposits);
        res.setTotalWithdrawals(withdrawals);
        res.setTransactionCount(transactions.size());
        res.setMostRecentTransactionDate(mostRecent);
        return res;
    }

    private TransactionResponse toResponse(Transaction tx) {
        TransactionResponse res = new TransactionResponse();
        res.setId(tx.getId());
        res.setFromAccount(tx.getFromAccount());
        res.setToAccount(tx.getToAccount());
        res.setAmount(tx.getAmount());
        res.setCurrency(tx.getCurrency());
        res.setType(tx.getType());
        res.setTimestamp(tx.getTimestamp());
        res.setStatus(tx.getStatus());
        return res;
    }
}
