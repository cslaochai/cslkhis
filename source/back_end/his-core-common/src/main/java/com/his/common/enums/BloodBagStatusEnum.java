package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 血袋状态枚举（血袋自身流转：待配血 → 已配血 → 已发血 → 已输注）。
 */
@Getter
public enum BloodBagStatusEnum {

    PENDING(0, "待配血"),
    CROSSMATCHED(1, "已配血"),
    ISSUED(2, "已发血"),
    INFUSED(3, "已输注");

    private final int code;
    private final String label;

    BloodBagStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * Integer 码值判定：null 安全，语义同 == 比较 int 常量
     */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }

    public static BloodBagStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (BloodBagStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        BloodBagStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        BloodBagStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        BloodBagStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
