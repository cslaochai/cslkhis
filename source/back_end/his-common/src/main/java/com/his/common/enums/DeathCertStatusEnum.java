package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 死亡证明状态枚举
 */
@Getter
public enum DeathCertStatusEnum {

    DRAFT(1, "草稿"),
    AUDITED(2, "已审核"),
    ISSUED(3, "已开具"),
    VOIDED(4, "已作废");

    private final int code;
    private final String label;

    DeathCertStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DeathCertStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DeathCertStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DeathCertStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DeathCertStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DeathCertStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
