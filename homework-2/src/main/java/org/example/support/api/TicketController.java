package org.example.support.api;

import jakarta.validation.Valid;
import org.example.support.api.dto.ClassificationResult;
import org.example.support.api.dto.ImportSummary;
import org.example.support.api.dto.TicketUpdateRequest;
import org.example.support.domain.Category;
import org.example.support.domain.Priority;
import org.example.support.domain.Ticket;
import org.example.support.service.TicketImportService;
import org.example.support.service.TicketClassifier;
import org.example.support.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService service;
    private final TicketImportService importService;
    private final TicketClassifier classifier;

    public TicketController(TicketService service, TicketImportService importService, TicketClassifier classifier) {
        this.service = service;
        this.importService = importService;
        this.classifier = classifier;
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody Ticket ticket,
                                          @RequestParam(name = "autoClassify", defaultValue = "false") boolean autoClassify) {
        Ticket created = service.create(ticket);
        if (autoClassify) {
            ClassificationResult res = classifier.classify(created);
            service.update(created.getId(), new TicketUpdateRequest()); // ensure updated state persisted
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/import")
    public ResponseEntity<ImportSummary> importTickets(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                                         @RequestParam(name = "format", required = false) String format) {
        try {
            String fname = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
            ImportSummary summary;
            String fmt = format != null ? format.toLowerCase() : (fname.endsWith(".csv") ? "csv" : (fname.endsWith(".json") ? "json" : (fname.endsWith(".xml") ? "xml" : "")));
            switch (fmt) {
                case "csv" -> summary = importService.importCsv(file.getInputStream());
                case "json" -> summary = importService.importJson(file.getInputStream());
                case "xml" -> summary = importService.importXml(file.getInputStream());
                default -> { return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); }
            }
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> listTickets(@RequestParam(required = false) Category category,
                                         @RequestParam(required = false) Priority priority,
                                         @RequestParam(required = false) org.example.support.domain.Status status,
                                         @RequestParam(required = false, name = "tag") String tag,
                                         @RequestParam(required = false, name = "from") String fromStr,
                                         @RequestParam(required = false, name = "to") String toStr) {
        java.time.Instant from = null;
        java.time.Instant to = null;
        try { if (fromStr != null) from = java.time.Instant.parse(fromStr); } catch (Exception ignored) {}
        try { if (toStr != null) to = java.time.Instant.parse(toStr); } catch (Exception ignored) {}
        List<Ticket> result = service.list(category, priority, status, tag, from, to);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicket(@PathVariable String id) {
        Ticket t = service.get(UUID.fromString(id));
        if (t == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(t);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable String id, @RequestBody TicketUpdateRequest updates) {
        Ticket updated = service.update(UUID.fromString(id), updates);
        if (updated == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable String id) {
        boolean deleted = service.delete(UUID.fromString(id));
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/{id}/auto-classify")
    public ResponseEntity<ClassificationResult> autoClassify(@PathVariable String id) {
        Ticket t = service.get(UUID.fromString(id));
        if (t == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        ClassificationResult res = classifier.classify(t);
        service.update(t.getId(), new TicketUpdateRequest());
        return ResponseEntity.ok(res);
    }
}
