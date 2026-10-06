package com.his.ai.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;

/**
 * 审计输出摘要组装器（G-11 口径）：模型输出 <b>不落原文</b>。
 * <p>
 * 摘要形态（按 DTO 字段声明顺序，分号拼接）：
 * <ul>
 *   <li>{@link AiAuditPlain} 标注的字段：过 {@link AiMaskUtils#mask} 后明文 —— 码值/判定结果，排障锚点</li>
 *   <li>未标注的 String 字段：{@code 字段名=SHA-256 前 12 位} —— 不可逆，但可核对「两次输出是否相同」</li>
 *   <li>集合字段：递归一层拼 POJO 元素（如 judgments 的每条判定），元素内字段按上述同规则；深度封顶防失控</li>
 *   <li>未标注的其他类型（数字/布尔）：直接取值</li>
 * </ul>
 * 非法反射访问、组装中途任何异常都按「返回空摘要」兜底 —— 审计失败不能反过来打断业务调用。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AiAuditDigestSupport {

    /**
     * 单个白名单字段的明文上限：码值/判定结果足够，临床文本本就不许标注
     */
    private static final int PLAIN_FIELD_MAX = 60;

    private static final int DIGEST_MAX = 480;

    private static final int FINGERPRINT_CHARS = 12;

    /**
     * 集合内 POJO 元素的递归层数：封顶 1，覆盖「judgments/焦点列表」这类一层嵌套契约
     */
    private static final int MAX_DEPTH = 1;

    /**
     * 组装模型输出 DTO 的审计摘要。
     *
     * @param parsed 结构化解析成功的输出对象（null 返回空串）
     */
    public static String buildDigest(Object parsed) {
        if (parsed == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try {
            appendFields(parsed.getClass(), parsed, sb, 0);
        } catch (Exception ignored) {
            return "";
        }
        String value = sb.toString();
        return value.length() <= DIGEST_MAX ? value : value.substring(0, DIGEST_MAX) + "...";
    }

    private static void appendFields(Class<?> type, Object target, StringBuilder sb, int depth)
            throws IllegalAccessException {
        for (Field field : type.getDeclaredFields()) {
            if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(target);
            if (value == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(field.getName()).append('=');
            appendValue(field, value, sb, depth);
        }
    }

    private static void appendValue(Field field, Object value, StringBuilder sb, int depth)
            throws IllegalAccessException {
        if (field.isAnnotationPresent(AiAuditPlain.class)) {
            String plain = String.valueOf(value);
            if (value instanceof String text) {
                plain = AiMaskUtils.mask(text).replaceAll("\\s+", " ").trim();
            }
            sb.append(plain.length() <= PLAIN_FIELD_MAX ? plain : plain.substring(0, PLAIN_FIELD_MAX) + "…");
            return;
        }
        if (value instanceof String text) {
            sb.append(sha256Short(text));
            return;
        }
        if (value instanceof Collection<?> coll) {
            if (depth < MAX_DEPTH && !coll.isEmpty() && isPlainPojo(coll.iterator().next())) {
                for (Object element : coll) {
                    sb.append('[');
                    appendFields(element.getClass(), element, sb, depth + 1);
                    sb.append(']');
                }
                return;
            }
            sb.append("size=").append(coll.size());
            return;
        }
        if (value instanceof Map<?, ?> map) {
            sb.append("size=").append(map.size());
            return;
        }
        sb.append(value);
    }

    /**
     * String 与数字等基础类型不按 POJO 递归（String 列表直接落 size，避免整串进摘要）
     */
    private static boolean isPlainPojo(Object value) {
        return value != null
                && !(value instanceof String)
                && !(value instanceof Number)
                && !(value instanceof Boolean)
                && !(value instanceof Collection<?>)
                && !(value instanceof Map<?, ?>);
    }

    /**
     * SHA-256 前 12 个十六进制位。同一段文本指纹恒相同 —— 排障时比对指纹即可判断两次输出是否一致。
     */
    public static String sha256Short(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
            }
            return hex.substring(0, FINGERPRINT_CHARS);
        } catch (NoSuchAlgorithmException e) {
            // JVM 必带 SHA-256，理论不可达；兜底成不可读标记而不是抛异常
            return "unavailable";
        }
    }
}
