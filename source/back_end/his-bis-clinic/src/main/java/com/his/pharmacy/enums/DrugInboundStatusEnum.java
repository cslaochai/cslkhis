package com.his.pharmacy.enums;

import lombok.Getter;

/**
 * 药品入库单状态
 */
@Getter
public enum DrugInboundStatusEnum {

    PENDING_AUDIT(1, "待审核"),
    AUDITED(2, "已审核"),
    INBOUND(3, "已入库"),
    CANCELLED(4, "已取消");

    private final Integer code;
    private final String label;

    DrugInboundStatusEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DrugInboundStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DrugInboundStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        DrugInboundStatusEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        DrugInboundStatusEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
