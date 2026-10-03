package com.his.patient.enums;

import lombok.Getter;

/**
 * 住院请假类别枚举
 */
@Getter
public enum InpatientLeaveTypeEnum {

    DAY_TRIP(1, "临时外出当日往返"),
    OVERNIGHT(2, "离院过夜"),
    OTHER(9, "其他");

    private final int code;
    private final String label;

    InpatientLeaveTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InpatientLeaveTypeEnum fromCode(int code) {
        for (InpatientLeaveTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案。 */
    public static String labelOf(Integer code) {
        InpatientLeaveTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
