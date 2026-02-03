package org.example.support.service;

import org.example.support.api.dto.ImportSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImportTests {

    @Autowired
    TicketImportService importService;

    @Test
    void importJsonSummarizesSuccessAndFailures() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/fixtures/sample_tickets.json")) {
            assertNotNull(in);
            ImportSummary summary = importService.importJson(in);
            assertEquals(3, summary.getTotalRecords());
            assertTrue(summary.getSuccessful() >= 2);
            assertTrue(summary.getFailed() >= 1);
            assertFalse(summary.getFailures().isEmpty());
        }
    }

    @Test
    void importCsvSummarizesSuccessAndFailures() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/fixtures/sample_tickets.csv")) {
            assertNotNull(in);
            ImportSummary summary = importService.importCsv(in);
            assertEquals(50, summary.getTotalRecords());
            assertTrue(summary.getSuccessful() >= 45, "Expected most tickets to import successfully");
            assertTrue(summary.getSuccessful() <= 50, "Successful count should not exceed total");
        }
    }

    @Test
    void importXmlParsesValidTickets() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/fixtures/valid_tickets.xml")) {
            assertNotNull(in);
            var summary = importService.importXml(in);
            assertEquals(30, summary.getTotalRecords());
            assertTrue(summary.getSuccessful() >= 25, "Expected most XML tickets to import successfully");
            assertEquals(0, summary.getFailed());
        }
    }
}
