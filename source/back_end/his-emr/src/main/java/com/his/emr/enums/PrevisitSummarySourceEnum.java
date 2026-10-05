package com.his.emr.enums;

import lombok.Getter;

/**
 * 预问诊病史摘要来源枚举
 */
@Getter
public enum PrevisitSummarySourceEnum {

    MODEL(1, "模型"),
    RULE(2, "规则");

    private final int code;
    private final String label;

    PrevisitSummarySourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PrevisitSummarySourceEnum fromCode(int code) {
        for (PrevisitSummarySourceEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案。 */
    public static String labelOf(Integer code) {
        PrevisitSummarySourceEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
