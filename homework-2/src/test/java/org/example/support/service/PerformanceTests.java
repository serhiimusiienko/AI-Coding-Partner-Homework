package org.example.support.service;

import org.example.support.domain.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}
