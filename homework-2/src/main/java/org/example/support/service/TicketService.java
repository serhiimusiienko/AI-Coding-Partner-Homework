package org.example.support.service;

import org.example.support.api.dto.TicketUpdateRequest;
import org.example.support.domain.Category;
import org.example.support.domain.Status;
import org.example.support.domain.Ticket;
import org.example.support.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {
    private final TicketRepository repository;

    public TicketService(TicketRepository repository) {
        this.repository = repository;
    }

    public Ticket create(Ticket ticket) {
        ticket.setId(UUID.randomUUID());
        ticket.setCreatedAt(Instant.now());
        ticket.setUpdatedAt(ticket.getCreatedAt());
        if (ticket.getStatus() == null) {
            ticket.setStatus(Status.NEW);
        }
        return repository.save(ticket);
    }

    public List<Ticket> list(Category category, org.example.support.domain.Priority priority) {
        return repository.findAll(category, priority);
    }

    public Ticket get(UUID id) {
        return repository.findById(id);
    }

    public Ticket update(UUID id, TicketUpdateRequest updates) {
        Ticket existing = repository.findById(id);
        if (existing == null) return null;

        if (updates.getCustomerId() != null) existing.setCustomerId(updates.getCustomerId());
        if (updates.getCustomerEmail() != null) existing.setCustomerEmail(updates.getCustomerEmail());
        if (updates.getCustomerName() != null) existing.setCustomerName(updates.getCustomerName());
        if (updates.getSubject() != null) existing.setSubject(updates.getSubject());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getCategory() != null) existing.setCategory(updates.getCategory());
        if (updates.getPriority() != null) existing.setPriority(updates.getPriority());
        if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
        if (updates.getResolvedAt() != null) existing.setResolvedAt(updates.getResolvedAt());
        if (updates.getAssignedTo() != null) existing.setAssignedTo(updates.getAssignedTo());
        if (updates.getTags() != null) existing.setTags(updates.getTags());
        if (updates.getMetadata() != null) existing.setMetadata(updates.getMetadata());
        if (updates.getClassificationConfidence() != null) existing.setClassificationConfidence(updates.getClassificationConfidence());

        existing.setUpdatedAt(Instant.now());
        return repository.save(existing);
    }

    public boolean delete(UUID id) {
        return repository.delete(id);
    }
}
