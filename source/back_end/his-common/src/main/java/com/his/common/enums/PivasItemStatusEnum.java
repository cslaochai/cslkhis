package com.his.common.enums;

import lombok.Getter;

/**
 * 静配中心调配明细状态枚举（医嘱 × 调配日：已拒配 / 待审方 / 已审方 / 已排队 / 已调配 / 已核对发放）。
 */
@Getter
public enum PivasItemStatusEnum {

    REJECTED(0, "已拒配"),
    PENDING_AUDIT(1, "待审方"),
    AUDITED(2, "已审方"),
    QUEUED(3, "已排队"),
    COMPOUNDED(4, "已调配"),
    VERIFIED(5, "已核对发放");

    private final int code;
    private final String label;

    PivasItemStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PivasItemStatusEnum fromCode(int code) {
        for (PivasItemStatusEnum item : values()) {
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
        PivasItemStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        PivasItemStatusEnum item = code == null ? null : fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
