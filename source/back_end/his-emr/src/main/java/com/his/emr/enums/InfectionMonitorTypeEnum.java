package com.his.emr.enums;

import lombok.Getter;

/**
 * 感染监测类型枚举
 */
@Getter
public enum InfectionMonitorTypeEnum {

    CAUTI(1, "尿管相关(CAUTI)"),
    CLABSI(2, "血管导管相关(CLABSI)"),
    VAP(3, "呼吸机相关(VAP)");

    private final int code;
    private final String label;

    InfectionMonitorTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InfectionMonitorTypeEnum fromCode(int code) {
        for (InfectionMonitorTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        InfectionMonitorTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        InfectionMonitorTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
