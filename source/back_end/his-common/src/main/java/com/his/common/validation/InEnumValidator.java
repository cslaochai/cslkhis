package com.his.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InEnum 校验器：反射调目标枚举的 isValid。
 */
public class InEnumValidator implements ConstraintValidator<InEnum, Object> {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(InEnumValidator.class);

    /**
     * 启动期只提醒一次同一个枚举，避免每请求刷屏
     */
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private Class<?> enumClass;
    private InEnum.Type type;
    private Method isValidMethod;
    private boolean resolvable;

    /**
     * 精确匹配参数类型，避免把 Integer 码值误喂给 isValid(String)。
     */
    private static Method resolveIsValid(Class<?> enumClass, InEnum.Type type) {
        Class<?> param = type == InEnum.Type.TEXT ? String.class : Integer.class;
        Method exact = find(enumClass, "isValid", param);
        if (exact != null) {
            return exact;
        }
        // 允许 int/Integer 互相兼容（枚举可能写的是 isValid(int)）
        for (Class<?> alt : List.of(param == String.class ? Integer.class : int.class,
                param == String.class ? int.class : String.class)) {
            Method m = find(enumClass, "isValid", alt);
            if (m != null) {
                return m;
            }
        }
        return null;
    }

    private static Method find(Class<?> enumClass, String name, Class<?> param) {
        try {
            Method m = enumClass.getMethod(name, param);
            return java.lang.reflect.Modifier.isStatic(m.getModifiers())
                    && m.getReturnType() == boolean.class ? m : null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Override
    public void initialize(InEnum constraintAnnotation) {
        this.enumClass = constraintAnnotation.value();
        this.type = constraintAnnotation.type();
        this.isValidMethod = resolveIsValid(enumClass, type);
        this.resolvable = isValidMethod != null;
        if (!resolvable && WARNED.add(enumClass.getName())) {
            log.warn("@InEnum 无法解析校验方法，已放行该字段，请给枚举 {} 补上 public static boolean isValid({})",
                    enumClass.getName(), type == InEnum.Type.TEXT ? "String" : "Integer");
        }
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // null 交给 @NotNull 管；解析不了方法也不拦（见类注释）
        if (value == null || !resolvable) {
            return true;
        }
        try {
            Object result = isValidMethod.invoke(null, value);
            return Boolean.TRUE.equals(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("@InEnum 调用 {}#isValid 失败，本次放行", enumClass.getName(), e);
            return true;
        }
    }
}
