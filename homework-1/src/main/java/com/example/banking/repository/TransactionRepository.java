package com.example.banking.repository;

import com.example.banking.model.Transaction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class TransactionRepository {
    private final ConcurrentMap<String, Transaction> store = new ConcurrentHashMap<>();

    public Transaction save(Transaction tx) {
        store.put(tx.getId(), tx);
        return tx;
    }

    public Optional<Transaction> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Transaction> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<Transaction> findByFilters(String accountId, String type, Instant from, Instant to) {
        return store.values().stream()
                .filter(tx -> accountId == null || accountId.equals(tx.getFromAccount()) || accountId.equals(tx.getToAccount()))
                .filter(tx -> type == null || type.equals(tx.getType()))
                .filter(tx -> from == null || !tx.getTimestamp().isBefore(from))
                .filter(tx -> to == null || !tx.getTimestamp().isAfter(to))
                .collect(Collectors.toList());
    }
}
