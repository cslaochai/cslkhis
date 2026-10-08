package com.his.common.enums;

import lombok.Getter;

/**
 * 出勤状态枚举（sql/200，字典 his_duty_status）
 */
@Getter
public enum StaffDutyStatusEnum {

    /**
     * 上班（带班次，计入在岗人数与工时）
     */
    WORK(1, "上班"),
    /**
     * 休息（轮休，无班次）
     */
    REST(2, "休息"),
    /**
     * 请假（年假/婚假/病假等，事由写备注）
     */
    LEAVE(3, "请假"),
    /**
     * 培训（院内外培训、进修）
     */
    TRAINING(4, "培训"),
    /**
     * 停班（临时停排，如封控、抽调）
     */
    SUSPENDED(5, "停班");

    private final int code;
    private final String label;

    StaffDutyStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StaffDutyStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StaffDutyStatusEnum status : values()) {
            if (status.code == code) {
                return status;
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
        StaffDutyStatusEnum status = fromCode(code);
        return status == null ? "未知(" + code + ")" : status.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        StaffDutyStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * 该状态是否计入在岗人数与工时。null 按不计入处理（脏数据不充数）。
     */
    public static boolean isWorking(Integer code) {
        return code != null && code == WORK.code;
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (StaffDutyStatusEnum status : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(status.code).append("-").append(status.label);
        }
        return sb.toString();
    }
}
