package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 死亡登记状态枚举
 */
@Getter
public enum DeathRegisterStatusEnum {

    DRAFT(1, "草稿"),
    DONE(2, "已登记"),
    VOIDED(3, "已作废");

    private final int code;
    private final String label;

    DeathRegisterStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DeathRegisterStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DeathRegisterStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DeathRegisterStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DeathRegisterStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DeathRegisterStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
