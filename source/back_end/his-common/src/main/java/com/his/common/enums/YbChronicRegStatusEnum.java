package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 门诊慢特病备案状态枚举（1-有效 2-已注销 3-已驳回）。
 * <p>
 * 只有 1-有效 参与「同患者同病种只许一条在有效期内」的判重与待遇计算；
 * 与慢病档案的认定状态是两个不同口径，不要互相套用。
 */
@Getter
public enum YbChronicRegStatusEnum {

    VALID(1, "有效"),
    CANCELLED(2, "已注销"),
    REJECTED(3, "已驳回");

    private final int code;
    private final String label;

    YbChronicRegStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static YbChronicRegStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (YbChronicRegStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        YbChronicRegStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        YbChronicRegStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        YbChronicRegStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
