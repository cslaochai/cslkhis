package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 抗菌药物处方权授权状态枚举（有效 / 暂停 / 取消）。
 */
@Getter
public enum AntibioticAuthStatusEnum {

    VALID(1, "有效"),
    PAUSED(2, "暂停"),
    REVOKED(3, "取消");

    private final int code;
    private final String label;

    AntibioticAuthStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static AntibioticAuthStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AntibioticAuthStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AntibioticAuthStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AntibioticAuthStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AntibioticAuthStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
