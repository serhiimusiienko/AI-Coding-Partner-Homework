package org.example.support.api.dto;

import org.example.support.domain.Category;
import org.example.support.domain.Metadata;
import org.example.support.domain.Priority;
import org.example.support.domain.Status;

import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public class TicketUpdateRequest {
    private String customerId;
    private String customerEmail;
    private String customerName;

    @Size(min = 1, max = 200)
    private String subject;

    @Size(min = 10, max = 2000)
    private String description;

    private Category category;
    private Priority priority;
    private Status status;

    private Instant resolvedAt;
    private String assignedTo;
    private List<String> tags;
    private Metadata metadata;
    private Double classificationConfidence;

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }
    public Double getClassificationConfidence() { return classificationConfidence; }
    public void setClassificationConfidence(Double classificationConfidence) { this.classificationConfidence = classificationConfidence; }
}
