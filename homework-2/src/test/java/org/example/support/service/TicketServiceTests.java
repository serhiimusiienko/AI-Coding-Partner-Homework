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
class TicketServiceTests {

    @Autowired
    TicketService service;

    // Tests from TicketServiceUpdateBranchTests
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

    // Tests from TicketServiceUpdateAdditionalTests
    @Test
    void updateResolvedAssignedTagsMetadataBranches() {
        Ticket t = new Ticket();
        t.setCustomerId("CUST-UPD");
        t.setCustomerEmail("upd@example.com");
        t.setCustomerName("Upd User");
        t.setSubject("Subject");
        t.setDescription("Description long enough for validation");
        t = service.create(t);

        Metadata meta = new Metadata();
        meta.setBrowser("Chrome");
        meta.setSource(Metadata.Source.WEB_FORM);
        meta.setDeviceType(Metadata.DeviceType.DESKTOP);

        TicketUpdateRequest req = new TicketUpdateRequest();
        req.setResolvedAt(Instant.now());
        req.setAssignedTo("Agent-1");
        req.setTags(List.of("vip","urgent"));
        req.setMetadata(meta);

        Ticket updated = service.update(t.getId(), req);
        assertNotNull(updated.getResolvedAt());
        assertEquals("Agent-1", updated.getAssignedTo());
        assertEquals(2, updated.getTags().size());
        assertNotNull(updated.getMetadata());
        assertNotNull(updated.getUpdatedAt());
    }

    // Tests from TicketServiceUpdateNegativeTests
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

    // Tests from TicketServiceUpdateCustomerFieldsTests
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
