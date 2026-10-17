package com.his.common.enums;

import lombok.Getter;

/**
 * 资金账户状态枚举（1-正常 2-冻结），账户主体自身的可用状态，与启用/停用、审核状态无关
 */
@Getter
public enum AccountStatusEnum {

    NORMAL(1, "正常"),
    FROZEN(2, "冻结");

    private final int code;
    private final String label;

    AccountStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AccountStatusEnum fromCode(int code) {
        for (AccountStatusEnum status : values()) {
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
        AccountStatusEnum status = code == null ? null : fromCode(code);
        return status == null ? "" : status.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        AccountStatusEnum status = code == null ? null : fromCode(code);
        if (status != null) {
            return status.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
