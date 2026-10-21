package com.his.common.enums;

import com.his.common.util.TextUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 医保审核证据判定结论
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

    public static EvidenceVerdictEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (EvidenceVerdictEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        EvidenceVerdictEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        EvidenceVerdictEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        EvidenceVerdictEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
