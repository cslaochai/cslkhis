package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 离院方式枚举
 */
@Getter
public enum DischargeWayEnum {

    MEDICAL_ORDER(1, "医嘱离院"),
    TRANSFER_HOSPITAL(2, "医嘱转院"),
    TRANSFER_COMMUNITY(3, "医嘱转社区"),
    UNMEDICAL(4, "非医嘱离院"),
    DEATH(5, "死亡"),
    OTHER(9, "其他");

    private final int code;
    private final String label;

    DischargeWayEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static DischargeWayEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DischargeWayEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DischargeWayEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DischargeWayEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DischargeWayEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
