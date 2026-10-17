package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 该状态是否计入在岗人数与工时。null 按不计入处理（脏数据不充数）。
     */
    public static boolean isWorking(Integer code) {
        return code != null && code == WORK.code;
    }

    public static StaffDutyStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (StaffDutyStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        StaffDutyStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        StaffDutyStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        StaffDutyStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
