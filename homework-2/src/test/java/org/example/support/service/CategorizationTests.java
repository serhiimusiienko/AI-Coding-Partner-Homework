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

    @Test
    void billingQuestionMediumPriority() {
        Ticket t = new Ticket();
        t.setSubject("Refund requested");
        t.setDescription("Customer requests invoice refund due to billing issue");

        var res = classifier.classify(t);
        assertEquals(org.example.support.domain.Category.BILLING_QUESTION, res.getCategory());
        assertEquals(org.example.support.domain.Priority.MEDIUM, res.getPriority());
    }

    @Test
    void featureRequestLowPriority() {
        Ticket t = new Ticket();
        t.setSubject("Feature request: dark mode");
        t.setDescription("Suggestion to add feature; cosmetic suggestion");

        var res = classifier.classify(t);
        assertEquals(org.example.support.domain.Category.FEATURE_REQUEST, res.getCategory());
        assertEquals(org.example.support.domain.Priority.LOW, res.getPriority());
    }

    @Test
    void bugReportHighPriorityWhenBlocking() {
        Ticket t = new Ticket();
        t.setSubject("Defect report");
        t.setDescription("Steps to reproduce defect; blocking deployment, important");

        var res = classifier.classify(t);
        assertEquals(org.example.support.domain.Category.BUG_REPORT, res.getCategory());
        assertEquals(org.example.support.domain.Priority.HIGH, res.getPriority());
    }

    @Test
    void otherCategoryWhenNoKeywords() {
        Ticket t = new Ticket();
        t.setSubject("General question");
        t.setDescription("Looking for guidance about product roadmap");

        var res = classifier.classify(t);
        assertEquals(org.example.support.domain.Category.OTHER, res.getCategory());
        assertEquals(org.example.support.domain.Priority.MEDIUM, res.getPriority());
    }
}
