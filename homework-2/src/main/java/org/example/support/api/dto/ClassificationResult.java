package org.example.support.api.dto;

import org.example.support.domain.Category;
import org.example.support.domain.Priority;

import java.util.List;

public class ClassificationResult {
    private Category category;
    private Priority priority;
    private double confidence;
    private String reasoning;
    private List<String> keywords;

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
}
