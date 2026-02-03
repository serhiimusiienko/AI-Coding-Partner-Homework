package org.example.support.repository;

import org.example.support.domain.Category;
import org.example.support.domain.Priority;
import org.example.support.domain.Status;
import org.example.support.domain.Ticket;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TicketRepository {
    private final Map<UUID, Ticket> store = new ConcurrentHashMap<>();

    public Ticket save(Ticket ticket) {
        store.put(ticket.getId(), ticket);
        return ticket;
    }

    public Ticket findById(UUID id) {
        return store.get(id);
    }

    public boolean delete(UUID id) {
        return store.remove(id) != null;
    }

    public List<Ticket> findAll(Category category, Priority priority, Status status, String tag, Instant from, Instant to) {
        List<Ticket> all = new ArrayList<>(store.values());
        return all.stream()
                .filter(t -> category == null || t.getCategory() == category)
                .filter(t -> priority == null || t.getPriority() == priority)
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> tag == null || (t.getTags() != null && t.getTags().contains(tag)))
                .filter(t -> from == null || (t.getCreatedAt() != null && !t.getCreatedAt().isBefore(from)))
                .filter(t -> to == null || (t.getCreatedAt() != null && !t.getCreatedAt().isAfter(to)))
                .collect(Collectors.toList());
    }
}
