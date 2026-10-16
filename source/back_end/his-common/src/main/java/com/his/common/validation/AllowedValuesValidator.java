package com.his.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AllowedValuesValidator implements ConstraintValidator<AllowedValues, Object> {

    private Set<String> allowed;
    private String separator;

    @Override
    public void initialize(AllowedValues annotation) {
        this.separator = annotation.separator();
        this.allowed = Arrays.stream(annotation.values())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        // 集合
        if (value instanceof Collection<?> coll) {
            for (Object item : coll) {
                if (item == null) continue;
                if (!checkSingle(item)) return false;
            }
            return true;
        }

        // 数组
        if (value.getClass().isArray()) {
            int len = Array.getLength(value);
            for (int i = 0; i < len; i++) {
                Object item = Array.get(value, i);
                if (item == null) continue;
                if (!checkSingle(item)) return false;
            }
            return true;
        }

        // 字符串（含逗号分隔多值）+ 其他任意单值，统一走 checkSingle
        return checkSingle(value);
    }

    private boolean checkSingle(Object item) {
        if (item instanceof CharSequence cs) {
            String str = cs.toString();
            if (str.isEmpty()) return true;
            if (separator != null && !separator.isEmpty() && str.contains(separator)) {
                for (String p : str.split(Pattern.quote(separator), -1)) {
                    if (p.isEmpty()) continue;
                    if (!allowed.contains(p)) return false;
                }
                return true;
            }
            return allowed.contains(str);
        }
        return allowed.contains(String.valueOf(item));
    }
}