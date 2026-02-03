package org.example.support.service;

import org.example.support.api.dto.TicketUpdateRequest;
import org.example.support.domain.Metadata;
import org.example.support.domain.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TicketServiceUpdateNegativeTests {

    @Autowired
    TicketService service;

    @Test
    void updateNonexistentReturnsNull() {
        TicketUpdateRequest req = new TicketUpdateRequest();
        assertNull(service.update(UUID.randomUUID(), req));
    }

    @Test
    void updateWithEmptyDtoDoesNotChangeFields() {
        Ticket t = new Ticket();
        t.setCustomerId("CUST-NEG-1");
        t.setCustomerEmail("neg1@example.com");
        t.setCustomerName("Neg One");
        t.setSubject("Subject");
        t.setDescription("Long enough description for validation");
        t.setTags(List.of("alpha"));
        Metadata md = new Metadata();
        md.setBrowser("Safari");
        t.setMetadata(md);

        Ticket created = service.create(t);
        Instant before = created.getUpdatedAt();

        TicketUpdateRequest req = new TicketUpdateRequest();
        Ticket updated = service.update(created.getId(), req);

        assertEquals("CUST-NEG-1", updated.getCustomerId());
        assertEquals("neg1@example.com", updated.getCustomerEmail());
        assertEquals("Neg One", updated.getCustomerName());
        assertEquals("Subject", updated.getSubject());
        assertEquals("Long enough description for validation", updated.getDescription());
        assertEquals(List.of("alpha"), updated.getTags());
        assertNotNull(updated.getMetadata());
        assertTrue(updated.getUpdatedAt().isAfter(before) || updated.getUpdatedAt().equals(before));
    }

    @Test
    void nullFieldsDoNotOverwriteExistingValues() {
        Ticket t = new Ticket();
        t.setCustomerId("CUST-NEG-2");
        t.setSubject("S");
        t.setDescription("Valid desc");
        t.setClassificationConfidence(0.7);
        t.setTags(List.of("beta"));
        Metadata md = new Metadata();
        md.setBrowser("Chrome");
        t.setMetadata(md);

        Ticket created = service.create(t);

        TicketUpdateRequest req = new TicketUpdateRequest();
        // leave tags/metadata/confidence null intentionally
        Ticket updated = service.update(created.getId(), req);

        assertEquals(List.of("beta"), updated.getTags());
        assertNotNull(updated.getMetadata());
        assertEquals(0.7, updated.getClassificationConfidence());
    }
}
