package com.his.ai.enums;

import lombok.Getter;

/**
 * 医保审核证据判定结论（G-07）。
 * <p>判的是「证据与规则怀疑的关系」，不是违规裁决；
 * {@link #labelOf(Integer)} 对不在枚举内的码值返回 null，脏数据由调用侧决定兜底，不许回落合法文案。</p>
 */
@Getter
public enum EvidenceVerdictEnum {

    /** 证据明确支持规则怀疑 */
    SUPPORTED(1, "证据支持"),

    /** 证据明确反驳规则怀疑 */
    REFUTED(2, "证据反驳"),

    /** 证据不足以判定 */
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
     * 模型输出按 verdict 字面匹配；大小写不敏感，匹配不上返回 null（调用侧丢弃该条）
     */
    public static EvidenceVerdictEnum fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (EvidenceVerdictEnum e : values()) {
            if (e.name().equalsIgnoreCase(name.trim())) {
                return e;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        EvidenceVerdictEnum e = fromCode(code);
        return e == null ? null : e.label;
    }
}
