package com.his.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class AllowedValuesValidator implements ConstraintValidator<AllowedValues, Object> {

    private Set<String> allowed;

    @Override
    public void initialize(AllowedValues annotation) {
        allowed = Arrays.stream(annotation.values())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return allowed.contains(String.valueOf(value));
    }
}