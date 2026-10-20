package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 规则判定结果 —— 刻意是三态，不是布尔。
 */
public enum AuditResultStateEnum {

    /**
     * 命中：规则判定存在问题
     */
    HIT(1, "命中"),

    /**
     * 通过：依据齐全，规则明确评估过且未发现问题
     */
    PASS(2, "通过"),

    /**
     * 不适用：缺少评估所需依据，<b>规则没有评估</b>（不得当作通过）
     */
    NOT_APPLICABLE(3, "不适用");

    private final int code;

    private final String label;

    AuditResultStateEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * @deprecated 用 {@link #fromCode(Integer)}（模板方法名）
     */
    @Deprecated
    public static AuditResultStateEnum of(Integer code) {
        return fromCode(code);
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static AuditResultStateEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AuditResultStateEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        if (null == code) {
            return "未评估";
        }
        AuditResultStateEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AuditResultStateEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AuditResultStateEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
