package org.example.support.service;

import org.example.support.api.dto.ClassificationResult;
import org.example.support.domain.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CategorizationTests {

    @Autowired
    TicketClassifier classifier;

    @Test
    void urgentTechnicalIssueIsDetected() {
        Ticket t = new Ticket();
        t.setCustomerId("CUST-100");
        t.setCustomerEmail("user@example.com");
        t.setCustomerName("User");
        t.setSubject("Production down");
        t.setDescription("App crash causes outage in production");

        ClassificationResult res = classifier.classify(t);
        assertEquals(org.example.support.domain.Category.TECHNICAL_ISSUE, res.getCategory());
        assertEquals(org.example.support.domain.Priority.URGENT, res.getPriority());
        assertTrue(res.getConfidence() >= 0.9);
        assertFalse(res.getKeywords().isEmpty());
    }
}
