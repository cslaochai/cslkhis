package com.his.patient.enums;

import lombok.Getter;

/**
 * 营养筛查量表枚举
 */
@Getter
public enum NutritionScreenTypeEnum {

    NRS2002(1, "NRS2002 营养风险筛查"),
    PG_SGA(2, "PG-SGA 患者参与主观整体评估"),
    MNA(3, "MNA 老年微型营养评估");

    private final int code;
    private final String label;

    NutritionScreenTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NutritionScreenTypeEnum fromCode(int code) {
        for (NutritionScreenTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String getText(Integer code) {
        NutritionScreenTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，
     * 用于业务异常消息或审计日志，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        NutritionScreenTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
