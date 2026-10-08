package com.his.common.enums;

import lombok.Getter;

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

    public static AttendModeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AttendModeEnum mode : values()) {
            if (mode.code == code) {
                return mode;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        AttendModeEnum mode = fromCode(code);
        return mode == null ? "未知(" + code + ")" : mode.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        AttendModeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * 该形态是否可以承载出诊（放号）。听班一律不放号；缺省按坐班处理。
     */
    public static boolean releasesSource(Integer code) {
        return code == null || code != ON_CALL.code;
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (AttendModeEnum mode : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(mode.code).append("-").append(mode.label);
        }
        return sb.toString();
    }
}
