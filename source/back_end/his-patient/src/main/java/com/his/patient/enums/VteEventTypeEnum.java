package com.his.patient.enums;

import lombok.Getter;

/**
 * VTE 事件类型枚举（文案与前端 {@code src/lib/vte.js} 逐字对齐，改这边必须改那边）
 */
@Getter
public enum VteEventTypeEnum {

    DVT(1, "深静脉血栓（DVT）"),
    PE(2, "肺栓塞（PE）"),
    BLEED(3, "预防相关出血");

    private final int code;
    private final String label;

    VteEventTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static VteEventTypeEnum fromCode(int code) {
        for (VteEventTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        VteEventTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        VteEventTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
