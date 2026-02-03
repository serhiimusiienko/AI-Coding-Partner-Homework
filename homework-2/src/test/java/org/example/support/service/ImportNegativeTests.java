package org.example.support.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImportNegativeTests {

    @Autowired
    TicketImportService importService;

    @Test
    void malformedCsvMissingHeaderLeadsToException() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/fixtures/malformed_tickets.csv")) {
            assertNotNull(in);
            assertThrows(Exception.class, () -> importService.importCsv(in));
        }
    }

    @Test
    void malformedXmlLeadsToException() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/fixtures/malformed_tickets.xml")) {
            assertNotNull(in);
            assertThrows(Exception.class, () -> importService.importXml(in));
        }
    }
}
