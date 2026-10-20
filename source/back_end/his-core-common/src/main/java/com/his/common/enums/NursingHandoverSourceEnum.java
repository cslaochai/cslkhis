package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 护理交班摘要来源枚举（1-模型 2-规则模板）。
 * <p>
 * 出参展示码：模型串过话才标 1，模型不可用时整段回落模板拼装的交班要素标 2。
 * 与「表达层来源」（字符串 rule/model）是两套码值，别互相套用；本档的判据是摘要文本谁写的。
 */
@Getter
public enum NursingHandoverSourceEnum {

    MODEL(1, "模型"),
    TEMPLATE(2, "规则模板");

    private final int code;
    private final String label;

    NursingHandoverSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static NursingHandoverSourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (NursingHandoverSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        NursingHandoverSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        NursingHandoverSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        NursingHandoverSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
