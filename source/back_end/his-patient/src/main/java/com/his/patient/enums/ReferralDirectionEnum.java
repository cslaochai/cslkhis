package com.his.patient.enums;

import lombok.Getter;

/**
 * 转诊方向枚举
 */
@Getter
public enum ReferralDirectionEnum {

    UP(1, "上转"),
    DOWN(2, "下转");

    private final int code;
    private final String label;

    ReferralDirectionEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ReferralDirectionEnum fromCode(int code) {
        for (ReferralDirectionEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String getText(Integer code) {
        ReferralDirectionEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        ReferralDirectionEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
