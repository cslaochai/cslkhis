package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 门诊慢特病备案的展示态枚举（1-有效 2-已注销 3-已驳回 4-已过期）。
 * <p>
 * 4-已过期是读取时按待遇终止日现算的派生档：库里只存 1-3，写侧一律不许用本枚举落库，
 * 否则「已过期」会变成一个人工可写的状态，判重闸与待遇计算都会被骗过去。
 */
@Getter
public enum YbChronicDisplayStatusEnum {

    VALID(1, "有效"),
    CANCELLED(2, "已注销"),
    REJECTED(3, "已驳回"),
    EXPIRED(4, "已过期");

    private final int code;
    private final String label;

    YbChronicDisplayStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static YbChronicDisplayStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (YbChronicDisplayStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        YbChronicDisplayStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        YbChronicDisplayStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        YbChronicDisplayStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
