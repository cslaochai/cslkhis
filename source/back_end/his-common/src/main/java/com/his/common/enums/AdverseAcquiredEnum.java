package com.his.common.enums;

import lombok.Getter;

/**
 * 不良事件来源枚举（sql/168，落在不良事件上报的获得方式列）
 */
@Getter
public enum AdverseAcquiredEnum {

    /**
     * 院内获得（进发生率分子）
     */
    HOSPITAL_ACQUIRED(1, "院内获得"),
    /**
     * 入院带入（留档不进分子）
     */
    ADMITTED_WITH(2, "入院带入");

    private final int code;
    private final String label;

    AdverseAcquiredEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AdverseAcquiredEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AdverseAcquiredEnum e : values()) {
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
        AdverseAcquiredEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        AdverseAcquiredEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
