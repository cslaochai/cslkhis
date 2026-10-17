package com.his.common.enums;

import lombok.Getter;

/**
 * 静配中心主单状态枚举（聚合派生：待审方 / 待排队 / 待调配 / 待核对 / 已完成 / 全拒配）。
 */
@Getter
public enum PivasBatchStatusEnum {

    PENDING_AUDIT(1, "待审方"),
    PENDING_QUEUE(2, "待排队"),
    PENDING_COMPOUND(3, "待调配"),
    PENDING_VERIFY(4, "待核对"),
    DONE(5, "已完成"),
    ALL_REJECTED(6, "全拒配");

    private final int code;
    private final String label;

    PivasBatchStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PivasBatchStatusEnum fromCode(int code) {
        for (PivasBatchStatusEnum item : values()) {
            if (item.code == code) return item;
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
        PivasBatchStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        PivasBatchStatusEnum item = code == null ? null : fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
