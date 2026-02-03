package org.example.support.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TicketModelValidationTests {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void invalidEmailFailsValidation() {
        Ticket t = new Ticket();
        t.setCustomerId("C1");
        t.setCustomerEmail("not-an-email");
        t.setCustomerName("User");
        t.setSubject("Valid subject");
        t.setDescription("Valid description text long enough");
        Set<?> violations = validator.validate(t);
        assertTrue(violations.stream().anyMatch(v -> v.toString().contains("customerEmail")));
    }

    @Test
    void shortSubjectFailsValidation() {
        Ticket t = new Ticket();
        t.setCustomerId("C1");
        t.setCustomerEmail("user@example.com");
        t.setCustomerName("User");
        t.setSubject("");
        t.setDescription("Valid description text long enough");
        Set<?> violations = validator.validate(t);
        assertTrue(violations.stream().anyMatch(v -> v.toString().contains("subject")));
    }

    @Test
    void validTicketPassesValidation() {
        Ticket t = new Ticket();
        t.setCustomerId("C1");
        t.setCustomerEmail("user@example.com");
        t.setCustomerName("User");
        t.setSubject("Login issue");
        t.setDescription("Cannot access account after password reset.");
        Set<?> violations = validator.validate(t);
        assertTrue(violations.isEmpty());
    }
}
