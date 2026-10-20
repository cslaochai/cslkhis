package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 角色数据权限范围枚举（1-全部数据 2-自定义数据 3-本部门数据 4-本部门及以下 5-仅本人数据）
 */
@Getter
public enum DataScopeEnum {

    ALL(1, "全部数据"),
    CUSTOM(2, "自定义数据"),
    DEPT(3, "本部门数据"),
    DEPT_AND_CHILD(4, "本部门及以下"),
    SELF(5, "仅本人数据");

    private final int code;
    private final String label;

    DataScopeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static DataScopeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DataScopeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DataScopeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DataScopeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DataScopeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
