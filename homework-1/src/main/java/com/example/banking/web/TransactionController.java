package com.example.banking.web;

import com.example.banking.dto.CreateTransactionRequest;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest req) {
        TransactionResponse res = service.createTransaction(req);
        return ResponseEntity.created(URI.create("/transactions/" + res.getId())).body(res);
    }

    @GetMapping
    public List<TransactionResponse> list(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return service.list(accountId, type, from, to);
    }

    @GetMapping("/{id}")
    public TransactionResponse get(@PathVariable String id) {
        return service.getById(id);
    }
}
