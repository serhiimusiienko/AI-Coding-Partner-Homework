package org.example.support.service;

import org.example.support.api.dto.TicketUpdateRequest;
import org.example.support.domain.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TicketServiceUpdateBranchTests {

    @Autowired
    TicketService service;

    @Test
    void updateClassificationConfidenceBranch() {
        Ticket t = new Ticket();
        t.setCustomerId("CUST-CONF");
        t.setCustomerEmail("conf@example.com");
        t.setCustomerName("Conf User");
        t.setSubject("Subject");
        t.setDescription("Description long enough for validation");
        t = service.create(t);

        TicketUpdateRequest req = new TicketUpdateRequest();
        req.setClassificationConfidence(0.77);
        Ticket updated = service.update(t.getId(), req);
        assertNotNull(updated);
        assertEquals(0.77, updated.getClassificationConfidence());
        assertNotNull(updated.getUpdatedAt());
    }
}
