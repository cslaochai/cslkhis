package com.his.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 码值合法性校验：值必须是目标枚举 {@code isValid(code)} 认可的码值。
 *
 * <p><b>为什么不用 @Min/@Max</b>：本仓码值绝大多数**不连续**（如维保类型只有 1/2/3，
 * 状态可能是 1/2/4），区间校验会放过 3 这种非法值。区间连续的码值优先用 @Min/@Max，
 * 不连续或来自字典的用本注解。
 *
 * <p><b>用法</b>：枚举需提供 {@code public static boolean isValid(Integer/String)}（AGENTS.md §13 枚举模板要求）。
 * <pre>
 * &#64;InEnum(EquipMaintainTypeEnum.class)
 * private Integer maintainType;
 * </pre>
 *
 * <p><b>null 一律放行</b>（是否必填交给 {@code @NotNull}），本注解只管「填了之后合不合法」。
 * 校验失败由 {@code GlobalExceptionHandler} 的 MethodArgumentNotValidException 分支兜成业务错误码，
 * 前端拿到的 message 即注解 message。
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InEnumValidator.class)
public @interface InEnum {

    /**
     * 目标枚举类，必须提供 {@code public static boolean isValid(Integer)} 或
     * {@code public static boolean isValid(String)} 静态方法。
     */
    Class<?> value();

    /**
     * 码值类型：CODE（Integer，默认）/ TEXT（String）。字符串码值枚举（如血型、反应类型）传 TEXT。
     */
    Type type() default Type.CODE;

    String message() default "码值取值不合法";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    enum Type {
        /** Integer 码值，枚举需有 isValid(Integer) */
        CODE,
        /** String 码值，枚举需有 isValid(String) */
        TEXT
    }
}
