package org.example.support.service;

import org.example.support.domain.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConcurrentOperationsTests {

    @Autowired
    TicketService service;

    @Test
    void createTwentyFiveTicketsConcurrently() throws Exception {
        int n = 25;
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<CompletableFuture<UUID>> futures = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int idx = i;
            futures.add(CompletableFuture.supplyAsync(() -> {
                Ticket t = new Ticket();
                t.setCustomerId("C" + idx);
                t.setCustomerEmail("user" + idx + "@example.com");
                t.setCustomerName("User" + idx);
                t.setSubject("S" + idx);
                t.setDescription("desc-" + idx + " - long enough");
                return service.create(t).getId();
            }, pool));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
        List<UUID> ids = futures.stream().map(CompletableFuture::join).toList();
        assertEquals(n, ids.size());
        assertEquals(ids.stream().distinct().count(), ids.size());
    }
}
