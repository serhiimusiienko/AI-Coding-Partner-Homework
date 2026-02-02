package org.example.support.repository;

import org.example.support.domain.Category;
import org.example.support.domain.Priority;
import org.example.support.domain.Ticket;
import org.springframework.stereotype.Repository;

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

    public List<Ticket> findAll(Category category, Priority priority) {
        List<Ticket> all = new ArrayList<>(store.values());
        return all.stream()
                .filter(t -> category == null || t.getCategory() == category)
                .filter(t -> priority == null || t.getPriority() == priority)
                .collect(Collectors.toList());
    }
}
