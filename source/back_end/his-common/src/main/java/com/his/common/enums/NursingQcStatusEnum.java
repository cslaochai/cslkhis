package com.his.common.enums;

import lombok.Getter;

/**
 * 护理质量检查单状态枚举（sql/168，落在护理质量检查单的状态列）
 */
@Getter
public enum NursingQcStatusEnum {

    /**
     * 草稿：可改明细
     */
    DRAFT(1, "草稿"),
    /**
     * 已确认：明细冻结，只能退回草稿后再改
     */
    CONFIRMED(2, "已确认");

    private final int code;
    private final String label;

    NursingQcStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NursingQcStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NursingQcStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        NursingQcStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        NursingQcStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
