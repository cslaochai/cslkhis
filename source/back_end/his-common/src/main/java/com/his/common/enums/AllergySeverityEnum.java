package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 过敏史严重程度枚举（轻度/中度/重度/危及生命/未评估）。
 * <p>
 * 「未评估」比列注释多一档，用于迁移生成与用户没选的场合 —— 说的是实话，不是编一个档位。
 * <p>
 * 码值本身即落库文案（中文串），与字典不是同一口径，改这里就是改数据。
 */
@Getter
public enum AllergySeverityEnum {

    MILD("轻度"),
    MODERATE("中度"),
    SEVERE("重度"),
    LIFE_THREATENING("危及生命"),
    UNASSESSED("未评估");

    private final String code;

    AllergySeverityEnum(String code) {
        this.code = code;
    }

    /**
     * 码值是否合法（写入侧校验用；null 与空串不合法）
     */
    public static boolean isValid(String code) {
        return code != null && !code.isEmpty() && fromCode(code) != null;
    }

    public static AllergySeverityEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (AllergySeverityEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        AllergySeverityEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.code;
    }

    public static String getLabel(String code, String defaultValue) {
        AllergySeverityEnum item = fromCode(code);
        return item == null ? defaultValue : item.code;
    }

    public static String getLabelOrUnknown(String code) {
        AllergySeverityEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.code;
    }
}
