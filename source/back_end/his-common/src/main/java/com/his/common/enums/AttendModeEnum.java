package com.his.common.enums;

import lombok.Getter;

/**
 * 响应形态枚举（sql/200，字典 {@code his_attend_mode}）
 *
 * <p><b>它回答的是「叫得动人叫不动」，出勤状态回答不了</b>：同样是「今天有班」，
 * 坐班的人就在单元里干活；听班的人在家待命，来电话才到岗；留院值班的人住在医院但不在门诊。
 * 这三档在派单、催班、急诊升级里的处置完全不同，所以是独立一维，不塞进入职状态里。
 *
 * <p><b>听班不放号</b>：听班 + 不出诊的组合不能生成号源——
 * 挂号系统给一个「在家待命、可能下午才到」的医生放号，患者到了没人看，
 * 是投诉而不是数据问题。判定走 {@link #releasesSource}。
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

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        AttendModeEnum mode = fromCode(code);
        return mode == null ? "未知(" + code + ")" : mode.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
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
