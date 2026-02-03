package org.example.support.service;

import org.example.support.api.dto.TicketUpdateRequest;
import org.example.support.domain.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TicketServiceUpdateCustomerFieldsTests {

    @Autowired
    TicketService service;

    @Test
    void updateCustomerFieldsBranches() {
        Ticket t = new Ticket();
        t.setCustomerId("CUST-UPD2");
        t.setCustomerEmail("upd2@example.com");
        t.setCustomerName("Upd Two");
        t.setSubject("Subject");
        t.setDescription("Long enough description for validation");
        t = service.create(t);

        TicketUpdateRequest req = new TicketUpdateRequest();
        req.setCustomerId("NEW-ID");
        req.setCustomerEmail("new@example.com");
        req.setCustomerName("New Name");

        Ticket updated = service.update(t.getId(), req);
        assertEquals("NEW-ID", updated.getCustomerId());
        assertEquals("new@example.com", updated.getCustomerEmail());
        assertEquals("New Name", updated.getCustomerName());
    }
}
