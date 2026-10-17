package com.his.common.enums;

import lombok.Getter;

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

    public static DoctorConsultStatusEnum fromCode(int code) {
        for (DoctorConsultStatusEnum status : values()) {
            if (status.code == code) return status;
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
        DoctorConsultStatusEnum status = code == null ? null : fromCode(code);
        return status == null ? "" : status.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        DoctorConsultStatusEnum status = code == null ? null : fromCode(code);
        if (status != null) {
            return status.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
