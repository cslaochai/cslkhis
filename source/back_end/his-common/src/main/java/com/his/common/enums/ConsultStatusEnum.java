package com.his.common.enums;

import lombok.Getter;

/**
 * 出诊计划的就诊状态枚举（这个班次此刻接不接患者，与停诊状态不是一件事）
 *
 * <p>停诊（{@link ScheduleStatusEnum#STOPPED}）说的是「号源池放不放号」；
 * 就诊状态说的是「已经开出的这个班，诊室里的接诊进行到哪一步」——
 * 停诊中的班次就诊状态必然停在待开始，但接诊中的班次仍可能被临时停诊（不再放新号）。
 */
@Getter
public enum ConsultStatusEnum {

    /** 待开始：班次尚未开诊 */
    NOT_STARTED(0, "待开始"),
    /** 接诊中：正在看诊 */
    CONSULTING(1, "接诊中"),
    /** 暂停：临时停接（医生离岗），恢复后继续 */
    PAUSED(2, "暂停");

    private final int code;
    private final String label;

    ConsultStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ConsultStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ConsultStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        ConsultStatusEnum status = fromCode(code);
        return status == null ? "未知(" + code + ")" : status.getLabel();
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (ConsultStatusEnum status : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(status.code).append("-").append(status.label);
        }
        return sb.toString();
    }
}
