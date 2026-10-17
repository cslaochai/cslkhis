package com.his.common.enums;

import lombok.Getter;

/**
 * 检查设备状态枚举（1-开放预约 2-暂停预约）
 */
@Getter
public enum ExamDeviceStatusEnum {

    OPEN(1, "开放预约"),
    PAUSED(2, "暂停预约");

    private final int code;
    private final String label;

    ExamDeviceStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ExamDeviceStatusEnum fromCode(int code) {
        for (ExamDeviceStatusEnum status : values()) {
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
        ExamDeviceStatusEnum status = code == null ? null : fromCode(code);
        return status == null ? "" : status.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        ExamDeviceStatusEnum status = code == null ? null : fromCode(code);
        if (status != null) {
            return status.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
