package com.his.common.enums;

import lombok.Getter;

/**
 * 岗位状态枚举（1-在职 2-已失效）。
 *
 * <p>这是<b>派生值</b>，不是可落库的列：到期与否永远由失效日期与当天现算，
 * 落列只会多出一个「日期已过、状态还没刷」的漂移窗口。
 */
@Getter
public enum EmployeePostStatusEnum {

    ACTIVE(1, "在职"),
    EXPIRED(2, "已失效");

    private final int code;
    private final String label;

    EmployeePostStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EmployeePostStatusEnum fromCode(int code) {
        for (EmployeePostStatusEnum status : values()) {
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
        EmployeePostStatusEnum status = code == null ? null : fromCode(code);
        return status == null ? "" : status.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        EmployeePostStatusEnum status = code == null ? null : fromCode(code);
        if (status != null) {
            return status.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
