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
            assertEquals(2, summary.getTotalRecords());
            assertEquals(1, summary.getSuccessful());
            assertEquals(1, summary.getFailed());
        }
    }
}
