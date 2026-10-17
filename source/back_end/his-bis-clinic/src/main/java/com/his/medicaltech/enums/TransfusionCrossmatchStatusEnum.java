package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 配血状态枚举（与输血流程状态分离：配血不合时流程停在 0，但这条必须能看见）。
 */
@Getter
public enum TransfusionCrossmatchStatusEnum {

    PENDING(0, "待配血"),
    PARTIAL(1, "配血中（未配齐）"),
    ALL_MATCHED(2, "全部相合"),
    INCOMPATIBLE(3, "存在配血不合");

    private final int code;
    private final String label;

    TransfusionCrossmatchStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TransfusionCrossmatchStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TransfusionCrossmatchStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 返回「—」；脏值返回空串。
     */
    public static String getText(Integer code) {
        if (code == null) {
            return "—";
        }
        TransfusionCrossmatchStatusEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」）。
     */
    public static String labelOrUnknown(Integer code) {
        TransfusionCrossmatchStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * Integer 码值判定：null 安全，语义同 == 比较 int 常量
     */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
