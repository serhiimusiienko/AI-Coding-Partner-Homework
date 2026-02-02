package org.example.support.api;

import jakarta.validation.Valid;
import org.example.support.api.dto.TicketUpdateRequest;
import org.example.support.domain.Category;
import org.example.support.domain.Priority;
import org.example.support.domain.Ticket;
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

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody Ticket ticket,
                                          @RequestParam(name = "autoClassify", defaultValue = "false") boolean autoClassify) {
        Ticket created = service.create(ticket);
        // autoClassify flag will be implemented later
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importTickets() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("TODO: importTickets");
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> listTickets(@RequestParam(required = false) Category category,
                                         @RequestParam(required = false) Priority priority) {
        List<Ticket> result = service.list(category, priority);
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
    public ResponseEntity<String> autoClassify(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("TODO: autoClassify");
    }
}
