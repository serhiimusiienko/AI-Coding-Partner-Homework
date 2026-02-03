package org.example.support.service;

import org.example.support.api.dto.TicketUpdateRequest;
import org.example.support.domain.Priority;
import org.example.support.domain.Status;
import org.example.support.domain.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PerformanceTests {

    @Autowired
    TicketService service;

    @Test
    void createTwoHundredTicketsUnderThreshold() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 200; i++) {
            Ticket t = new Ticket();
            t.setCustomerId("PC" + i);
            t.setCustomerEmail("perf" + i + "@example.com");
            t.setCustomerName("Perf" + i);
            t.setSubject("Subject " + i);
            t.setDescription("Description long enough " + i);
            service.create(t);
        }
        long duration = System.currentTimeMillis() - start;
        assertTrue(duration < 5000, "Creation of 200 tickets took too long: " + duration + "ms");
    }

    @Test
    void bulkListOperationUnderThreshold() {
        // Create some test data
        for (int i = 0; i < 50; i++) {
            Ticket t = new Ticket();
            t.setCustomerId("BL" + i);
            t.setCustomerEmail("bulk" + i + "@example.com");
            t.setCustomerName("Bulk" + i);
            t.setSubject("Bulk subject " + i);
            t.setDescription("Bulk description sufficient length " + i);
            service.create(t);
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            List<Ticket> results = service.list(null, null, null, null, null, null);
            assertNotNull(results);
        }
        long duration = System.currentTimeMillis() - start;
        assertTrue(duration < 2000, "100 list operations took too long: " + duration + "ms");
    }

    @Test
    void updateOperationsPerformance() {
        // Create tickets to update
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Ticket t = new Ticket();
            t.setCustomerId("UP" + i);
            t.setCustomerEmail("update" + i + "@example.com");
            t.setCustomerName("Update" + i);
            t.setSubject("Update subject " + i);
            t.setDescription("Update description sufficient length " + i);
            tickets.add(service.create(t));
        }

        long start = System.currentTimeMillis();
        for (Ticket t : tickets) {
            TicketUpdateRequest update = new TicketUpdateRequest();
            update.setSubject("Updated subject");
            update.setStatus(Status.IN_PROGRESS);
            service.update(t.getId(), update);
        }
        long duration = System.currentTimeMillis() - start;
        assertTrue(duration < 1000, "100 update operations took too long: " + duration + "ms");
    }

    @Test
    void concurrentReadOperationsPerformance() throws Exception {
        // Create test data
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Ticket t = new Ticket();
            t.setCustomerId("CR" + i);
            t.setCustomerEmail("concurrent" + i + "@example.com");
            t.setCustomerName("Concurrent" + i);
            t.setSubject("Concurrent subject " + i);
            t.setDescription("Concurrent description sufficient length " + i);
            tickets.add(service.create(t));
        }

        ExecutorService executor = Executors.newFixedThreadPool(20);
        long start = System.currentTimeMillis();
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final int index = i % tickets.size();
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Ticket result = service.get(tickets.get(index).getId());
                assertNotNull(result);
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        long duration = System.currentTimeMillis() - start;
        assertTrue(duration < 1000, "20 concurrent read operations took too long: " + duration + "ms");
    }

    @Test
    void filteringOperationsPerformance() {
        // Create diverse test data
        for (int i = 0; i < 50; i++) {
            Ticket t = new Ticket();
            t.setCustomerId("FP" + i);
            t.setCustomerEmail("filter" + i + "@example.com");
            t.setCustomerName("Filter" + i);
            t.setSubject("Filter subject " + i);
            t.setDescription("Filter description sufficient length " + i);
            if (i % 2 == 0) {
                t.setPriority(Priority.HIGH);
            } else {
                t.setPriority(Priority.LOW);
            }
            service.create(t);
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
            List<Ticket> highPriority = service.list(null, Priority.HIGH, null, null, null, null);
            List<Ticket> lowPriority = service.list(null, Priority.LOW, null, null, null, null);
            assertNotNull(highPriority);
            assertNotNull(lowPriority);
        }
        long duration = System.currentTimeMillis() - start;
        assertTrue(duration < 1500, "100 filtering operations took too long: " + duration + "ms");
    }
}
