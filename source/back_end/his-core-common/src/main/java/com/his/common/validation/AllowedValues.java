package com.his.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AllowedValuesValidator.class)
public @interface AllowedValues {

    String message() default "值不在允许范围内";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 允许的值
     */
    String[] values() default {};

    /**
     * 字符串多值分隔符，默认英文逗号
     */
    String separator() default ",";

    /**
     * 是否对每个值去首尾空白
     */
    boolean trim() default true;
}