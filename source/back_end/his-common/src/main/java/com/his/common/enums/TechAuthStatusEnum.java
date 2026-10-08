package com.his.common.enums;

import lombok.Getter;

/**
 * 技术授权状态枚举（sql/155）。
 */
@Getter
public enum TechAuthStatusEnum {

    /**
     * 待审批：科室已提交，委员会未批，闸门不认
     */
    PENDING(1, "待审批"),
    /**
     * 已授权：唯一放行的状态
     */
    GRANTED(2, "已授权"),
    /**
     * 已驳回：审批未通过
     */
    REJECTED(3, "已驳回"),
    /**
     * 已收回：动态调整 / 年度再授权未通过 / 事件触发
     */
    REVOKED(4, "已收回");

    private final int code;
    private final String label;

    TechAuthStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TechAuthStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TechAuthStatusEnum status : values()) {
            if (status.code == code) {
                return status;
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
        TechAuthStatusEnum status = fromCode(code);
        return status == null ? "未知(" + code + ")" : status.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        TechAuthStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
