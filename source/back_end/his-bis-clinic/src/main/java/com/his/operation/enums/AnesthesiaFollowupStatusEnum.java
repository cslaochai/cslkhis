package com.his.operation.enums;

import lombok.Getter;

/**
 * 麻醉随访状态枚举（码值口径 = 麻醉随访单随访状态字段的列注释）。
 */
@Getter
public enum AnesthesiaFollowupStatusEnum {

    DRAFT(0, "草稿"),
    DONE(1, "已完成");

    private final int code;
    private final String label;

    AnesthesiaFollowupStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AnesthesiaFollowupStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AnesthesiaFollowupStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        AnesthesiaFollowupStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        AnesthesiaFollowupStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
