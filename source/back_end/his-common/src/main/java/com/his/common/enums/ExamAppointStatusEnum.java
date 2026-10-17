package com.his.common.enums;

import lombok.Getter;

/**
 * 检查预约状态枚举（1-已预约 2-已到检 3-已完成 4-已取消 5-爽约）
 */
@Getter
public enum ExamAppointStatusEnum {

    BOOKED(1, "已预约"),
    ARRIVED(2, "已到检"),
    FINISHED(3, "已完成"),
    CANCELLED(4, "已取消"),
    NOSHOW(5, "爽约");

    private final int code;
    private final String label;

    ExamAppointStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ExamAppointStatusEnum fromCode(int code) {
        for (ExamAppointStatusEnum status : values()) {
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
        ExamAppointStatusEnum status = code == null ? null : fromCode(code);
        return status == null ? "" : status.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        ExamAppointStatusEnum status = code == null ? null : fromCode(code);
        if (status != null) {
            return status.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
