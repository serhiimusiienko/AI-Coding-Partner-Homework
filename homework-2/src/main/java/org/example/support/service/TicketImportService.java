package org.example.support.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.example.support.api.dto.ImportFailureDetail;
import org.example.support.api.dto.ImportSummary;
import org.example.support.domain.Ticket;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class TicketImportService {
    private final TicketService ticketService;
    private final Validator validator;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;

    public TicketImportService(TicketService ticketService, Validator validator, ObjectMapper objectMapper) {
        this.ticketService = ticketService;
        this.validator = validator;
        this.objectMapper = objectMapper;
        this.xmlMapper = new XmlMapper();
    }

    public ImportSummary importJson(InputStream in) throws IOException {
        List<Ticket> tickets = objectMapper.readValue(in, new TypeReference<List<Ticket>>(){});
        return persistWithValidation(tickets);
    }

    public ImportSummary importXml(InputStream in) throws IOException {
        // Expect XML as a root list <tickets><ticket>...</ticket></tickets>
        // Map to List<Ticket>
        List<Ticket> tickets = xmlMapper.readValue(in, new TypeReference<List<Ticket>>(){});
        return persistWithValidation(tickets);
    }

    public ImportSummary importCsv(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        CSVParser parser = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(reader);

        List<Ticket> tickets = new ArrayList<>();
        for (CSVRecord r : parser) {
            Ticket t = new Ticket();
            // Expect headers matching snake_case names
            t.setCustomerId(r.get("customer_id"));
            t.setCustomerEmail(r.get("customer_email"));
            t.setCustomerName(r.get("customer_name"));
            t.setSubject(r.get("subject"));
            t.setDescription(r.get("description"));
            // category/priority/status are optional; rely on defaults if empty
            String category = getOptional(r, "category");
            if (category != null && !category.isBlank()) {
                try { t.setCategory(Enum.valueOf(org.example.support.domain.Category.class, category.toUpperCase())); } catch (IllegalArgumentException ignored) {}
            }
            String priority = getOptional(r, "priority");
            if (priority != null && !priority.isBlank()) {
                try { t.setPriority(Enum.valueOf(org.example.support.domain.Priority.class, priority.toUpperCase())); } catch (IllegalArgumentException ignored) {}
            }
            String status = getOptional(r, "status");
            if (status != null && !status.isBlank()) {
                try { t.setStatus(Enum.valueOf(org.example.support.domain.Status.class, status.toUpperCase())); } catch (IllegalArgumentException ignored) {}
            }
            tickets.add(t);
        }
        return persistWithValidation(tickets);
    }

    private String getOptional(CSVRecord r, String name) {
        try { return r.get(name); } catch (IllegalArgumentException e) { return null; }
    }

    private ImportSummary persistWithValidation(List<Ticket> tickets) {
        ImportSummary summary = new ImportSummary();
        summary.setTotalRecords(tickets.size());
        int ok = 0;
        int fail = 0;
        int idx = 0;
        for (Ticket t : tickets) {
            Set<ConstraintViolation<Ticket>> errors = validator.validate(t);
            if (!errors.isEmpty()) {
                fail++;
                String msg = String.join("; ", errors.stream().map(e -> e.getPropertyPath()+": "+e.getMessage()).toList());
                summary.addFailure(new ImportFailureDetail(idx, msg));
            } else {
                ticketService.create(t);
                ok++;
            }
            idx++;
        }
        summary.setSuccessful(ok);
        summary.setFailed(fail);
        return summary;
    }
}
