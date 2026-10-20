package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 响应形态枚举（sql/200，字典 his_attend_mode）
 */
@Getter
public enum AttendModeEnum {

    /**
     * 坐班：正常到岗在单元内工作
     */
    ON_SITE(1, "坐班"),
    /**
     * 听班：待命响应，呼叫到岗（二线/三线）
     */
    ON_CALL(2, "听班"),
    /**
     * 留院值班：在院住宿舍，负责夜间与节假日
     */
    IN_HOSPITAL(3, "留院值班");

    private final int code;
    private final String label;

    AttendModeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 该形态是否可以承载出诊（放号）。听班一律不放号；缺省按坐班处理。
     */
    public static boolean releasesSource(Integer code) {
        return code == null || code != ON_CALL.code;
    }

    public static AttendModeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AttendModeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AttendModeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AttendModeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AttendModeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
