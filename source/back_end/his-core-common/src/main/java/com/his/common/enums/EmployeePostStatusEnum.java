package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 岗位状态枚举（1-在职 2-已失效）。
 *
 * <p>这是<b>派生值</b>，不是可落库的列：到期与否永远由失效日期与当天现算，
 * 落列只会多出一个「日期已过、状态还没刷」的漂移窗口。
 */
@Getter
public enum EmployeePostStatusEnum {

    ACTIVE(1, "在职"),
    EXPIRED(2, "已失效");

    private final int code;
    private final String label;

    EmployeePostStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static EmployeePostStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (EmployeePostStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        EmployeePostStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        EmployeePostStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        EmployeePostStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
