package com.his.common.enums;

import lombok.Getter;

/**
 * 医保飞检批次状态枚举（1-进行中 2-已结项 3-已作废）
 */
@Getter
public enum YbInspectionStatusEnum {

    RUNNING(1, "进行中"),
    CONCLUDED(2, "已结项"),
    CANCELLED(3, "已作废");

    private final int code;
    private final String label;

    YbInspectionStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static YbInspectionStatusEnum fromCode(int code) {
        for (YbInspectionStatusEnum status : values()) {
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
        YbInspectionStatusEnum status = code == null ? null : fromCode(code);
        return status == null ? "" : status.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        YbInspectionStatusEnum status = code == null ? null : fromCode(code);
        if (status != null) {
            return status.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
