package org.example.support.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody String body,
                                          @RequestParam(name = "autoClassify", defaultValue = "false") boolean autoClassify) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("TODO: createTicket");
    }

    @PostMapping("/import")
    public ResponseEntity<?> importTickets() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("TODO: importTickets");
    }

    @GetMapping
    public ResponseEntity<?> listTickets(@RequestParam(required = false) String category,
                                         @RequestParam(required = false) String priority) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("TODO: listTickets");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTicket(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("TODO: getTicket");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTicket(@PathVariable String id, @RequestBody String body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("TODO: updateTicket");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("TODO: deleteTicket");
    }

    @PostMapping("/{id}/auto-classify")
    public ResponseEntity<?> autoClassify(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("TODO: autoClassify");
    }
}
