package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 医生在线接诊状态（缓存里的即时在岗状态，按「医生 + 当天」存，不是落库列）。
 *
 * <p>与出诊班次上的就诊状态是两套口径：那边回答「这个班次此刻接不接诊（0-待开始）」，
 * 这里回答「这个医生此刻是不是正在看诊（0-空闲，可被叫号）」，码值相同但含义不同，
 * 所以是两个枚举，不要合并。
 */
@Getter
public enum DoctorConsultStatusEnum {

    IDLE(0, "空闲"),
    CONSULTING(1, "接诊中"),
    PAUSED(2, "暂停");

    private final int code;
    private final String label;

    DoctorConsultStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static DoctorConsultStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DoctorConsultStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DoctorConsultStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DoctorConsultStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DoctorConsultStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
