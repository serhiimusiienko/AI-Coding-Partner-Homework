package com.example.banking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class MaxTwoDecimalPlacesValidator implements ConstraintValidator<MaxTwoDecimalPlaces, BigDecimal> {
    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) return true;
        if (value.signum() <= 0) return false;
        return value.scale() <= 2;
    }
}
