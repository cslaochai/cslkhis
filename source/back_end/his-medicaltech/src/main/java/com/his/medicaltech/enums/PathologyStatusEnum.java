package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 病理单状态枚举（码值口径 = biz_pathology_order.status 列注释）。
 *
 * <p>文案供后端拼提示用，页面渲染仍走字典 {@code DictCacheService.text}（his_pathology_status）。
 */
@Getter
public enum PathologyStatusEnum {

    REGISTERED(1, "已登记"),
    RECEIVED(2, "已接收"),
    SAMPLED(3, "已取材"),
    SLICED(4, "已切片"),
    REPORTED(5, "已出报告"),
    AUDITED(6, "已审核"),
    PUBLISHED(7, "已发布"),
    CANCELLED(8, "已取消");

    private final int code;
    private final String label;

    PathologyStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PathologyStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PathologyStatusEnum e : values()) {
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

    /**
     * 展示用：null 或不在枚举内返回空串（不把「未知」渲染给用户看）
     */
    public static String getText(Integer code) {
        PathologyStatusEnum e = fromCode(code);
        return e == null ? "" : e.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        PathologyStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * Integer 码值判定：null 安全，语义同 == 比较 int 常量
     */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
