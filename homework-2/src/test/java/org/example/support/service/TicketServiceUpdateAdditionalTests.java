package org.example.support.service;

import org.example.support.api.dto.TicketUpdateRequest;
import org.example.support.domain.Metadata;
import org.example.support.domain.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TicketServiceUpdateAdditionalTests {

    @Autowired
    TicketService service;

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
}
