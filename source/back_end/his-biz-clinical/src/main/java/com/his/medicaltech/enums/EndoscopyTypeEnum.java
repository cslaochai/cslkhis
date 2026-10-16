package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 内镜检查类型枚举
 */
@Getter
public enum EndoscopyTypeEnum {

    GASTRO(1, "胃镜"),
    COLON(2, "肠镜"),
    ERCP(7, "ERCP");

    private final int code;
    private final String label;

    EndoscopyTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EndoscopyTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EndoscopyTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）。注意本枚举只收代码要判定的三类，不是全量类型字典。
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用：null 或不在枚举内返回空串（全量文案以字典为准，本枚举只覆盖代码判定过的那三类）
     */
    public static String getText(Integer code) {
        EndoscopyTypeEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        EndoscopyTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * Integer 码值判定：null 安全，语义同 == 比较 int 常量
     */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
