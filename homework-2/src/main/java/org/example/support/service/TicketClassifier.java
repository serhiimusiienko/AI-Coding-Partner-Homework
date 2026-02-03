package org.example.support.service;

import org.example.support.api.dto.ClassificationResult;
import org.example.support.domain.Category;
import org.example.support.domain.Priority;
import org.example.support.domain.Ticket;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class TicketClassifier {
    private final List<String> decisionLog = new ArrayList<>();

    public ClassificationResult classify(Ticket t) {
        String text = (t.getSubject() + "\n" + t.getDescription()).toLowerCase(Locale.ROOT);
        List<String> keywords = new ArrayList<>();

        Category category = Category.OTHER;
        if (containsAny(text, keywords, new String[]{"login","password","2fa","sign in","account"})) {
            category = Category.ACCOUNT_ACCESS;
        } else if (containsAny(text, keywords, new String[]{"error","crash","bug","exception","fails"})) {
            category = Category.TECHNICAL_ISSUE;
        } else if (containsAny(text, keywords, new String[]{"invoice","payment","refund","billing"})) {
            category = Category.BILLING_QUESTION;
        } else if (containsAny(text, keywords, new String[]{"feature","request","enhancement","suggestion"})) {
            category = Category.FEATURE_REQUEST;
        } else if (containsAny(text, keywords, new String[]{"bug","defect","reproduce"})) {
            category = Category.BUG_REPORT;
        }

        Priority priority = Priority.MEDIUM;
        double score = 0.5;
        if (containsAny(text, keywords, new String[]{"can't access","critical","production down","security"})) {
            priority = Priority.URGENT; score = 0.95;
        } else if (containsAny(text, keywords, new String[]{"important","blocking","asap"})) {
            priority = Priority.HIGH; score = 0.8;
        } else if (containsAny(text, keywords, new String[]{"minor","cosmetic","suggestion"})) {
            priority = Priority.LOW; score = 0.6;
        }

        String reasoning = "Category determined by keyword match; priority by urgency terms.";
        ClassificationResult result = new ClassificationResult();
        result.setCategory(category);
        result.setPriority(priority);
        result.setConfidence(score);
        result.setReasoning(reasoning);
        result.setKeywords(keywords);

        // update ticket and log decision
        t.setCategory(category);
        t.setPriority(priority);
        t.setClassificationConfidence(score);
        decisionLog.add("ticket=" + t.getId() + ": " + category + ", " + priority + ", conf=" + score);
        return result;
    }

    public List<String> getDecisionLog() { return decisionLog; }

    private boolean containsAny(String text, List<String> keywords, String[] terms) {
        boolean matched = false;
        for (String term : terms) {
            if (text.contains(term)) { keywords.add(term); matched = true; }
        }
        return matched;
    }
}
