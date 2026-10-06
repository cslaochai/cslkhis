package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 血库出入库流水业务类型枚举
 */
@Getter
public enum BloodStockLogBizTypeEnum {

    INBOUND(1, "入库"),
    ISSUE(2, "发血"),
    RETURN(3, "退回"),
    SCRAP(4, "报废"),
    RESERVE(5, "预留"),
    CANCEL_RESERVE(6, "取消预留");

    private final int code;
    private final String label;

    BloodStockLogBizTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BloodStockLogBizTypeEnum fromCode(int code) {
        for (BloodStockLogBizTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /** 展示用：null 或不在枚举内返回空串（不把「未知」渲染给用户看） */
    public static String getText(Integer code) {
        BloodStockLogBizTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        BloodStockLogBizTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
