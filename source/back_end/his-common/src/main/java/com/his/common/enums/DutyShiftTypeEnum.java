package com.his.common.enums;

import lombok.Getter;

/**
 * 值守班段枚举（字典 {@code his_duty_shift}）
 *
 * <p>它只回答「这一班是白段还是夜段」，具体起止时刻由值守册的班次带出
 * （夜段就是那条跨零点的 18:00~次日 08:00，白段是同日起止的那条）。
 * 分成两档而不是直接用车间时刻，是因为「今天谁负责」的升级链路要按段找人：
 * 凌晨 2 点的责任人写在<b>昨天</b>的夜段上，按时刻查会查出一个刚上班的人。
 */
@Getter
public enum DutyShiftTypeEnum {

    /** 白段：白天接管全院应急协调 */
    DAY(1, "白班"),
    /** 夜段：跨零点，归开始日 */
    NIGHT(2, "夜班");

    private final int code;
    private final String label;

    DutyShiftTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DutyShiftTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DutyShiftTypeEnum shift : values()) {
            if (shift.code == code) {
                return shift;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        DutyShiftTypeEnum shift = fromCode(code);
        return shift == null ? null : shift.getLabel();
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (DutyShiftTypeEnum shift : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(shift.code).append("-").append(shift.label);
        }
        return sb.toString();
    }
}
