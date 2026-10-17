package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 护理排班单元类型枚举（1-病区 2-门诊科室）。
 *
 * <p>注意与组织单元类型（OrgUnitTypeEnum，1-科室 2-病区）<b>码值相反</b>：
 * 这是护理排班页的「排班单元」入参口径，落在底座前必须经换算，禁止直接互传。
 */
@Getter
public enum NurseScheduleUnitTypeEnum {

    WARD(1, "病区"),
    CLINIC(2, "门诊科室");

    private final int code;
    private final String label;

    NurseScheduleUnitTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static NurseScheduleUnitTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (NurseScheduleUnitTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        NurseScheduleUnitTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        NurseScheduleUnitTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        NurseScheduleUnitTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
