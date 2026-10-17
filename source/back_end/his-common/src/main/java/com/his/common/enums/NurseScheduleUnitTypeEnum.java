package com.his.common.enums;

import lombok.Getter;

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

    public static NurseScheduleUnitTypeEnum fromCode(int code) {
        for (NurseScheduleUnitTypeEnum type : values()) {
            if (type.code == code) return type;
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        NurseScheduleUnitTypeEnum type = code == null ? null : fromCode(code);
        return type == null ? "" : type.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        NurseScheduleUnitTypeEnum type = code == null ? null : fromCode(code);
        if (type != null) {
            return type.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (NurseScheduleUnitTypeEnum type : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(type.code).append("-").append(type.label);
        }
        return sb.toString();
    }
}
