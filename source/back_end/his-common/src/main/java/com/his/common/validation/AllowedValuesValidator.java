package com.his.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class AllowedValuesValidator implements ConstraintValidator<AllowedValues, Integer> {

    private Set<Integer> allowed;

    @Override
    public void initialize(AllowedValues annotation) {
        allowed = Arrays.stream(annotation.values())
                        .boxed()
                        .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) return true;   // 空值交给 @NotNull 管
        return allowed.contains(value);
    }
}