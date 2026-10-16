package com.his.ai.enums;

import com.his.common.util.TextUtil;
import lombok.Getter;

/**
 * 医保审核证据判定结论（G-07）。
 */
@Getter
public enum EvidenceVerdictEnum {

    /**
     * 证据明确支持规则怀疑
     */
    SUPPORTED(1, "证据支持"),

    /**
     * 证据明确反驳规则怀疑
     */
    REFUTED(2, "证据反驳"),

    /**
     * 证据不足以判定
     */
    INSUFFICIENT(3, "证据不足");

    private final Integer code;
    private final String label;

    EvidenceVerdictEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EvidenceVerdictEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EvidenceVerdictEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 模型输出按 verdict 字面匹配；大小写不敏感，匹配不上返回 null（调用侧丢弃该条）
     */
    public static EvidenceVerdictEnum fromName(String name) {
        if (!TextUtil.hasText(name)) {
            return null;
        }
        for (EvidenceVerdictEnum e : values()) {
            if (e.name().equalsIgnoreCase(name.trim())) {
                return e;
            }
        }
        return null;
    }

    public static String getText(Integer code) {
        EvidenceVerdictEnum e = fromCode(code);
        return e == null ? null : e.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        EvidenceVerdictEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
