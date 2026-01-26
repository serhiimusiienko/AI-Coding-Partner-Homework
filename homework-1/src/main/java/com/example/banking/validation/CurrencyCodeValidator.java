package com.example.banking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class CurrencyCodeValidator implements ConstraintValidator<CurrencyCode, String> {
    private static final Set<String> CODES = Set.of(
            "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        return CODES.contains(value.toUpperCase());
    }
}
