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

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案。 */
    public static String labelOf(Integer code) {
        InfectionMonitorTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
