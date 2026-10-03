package com.his.patient.enums;

import lombok.Getter;

/**
 * 护理文书类型枚举
 */
@Getter
public enum NursingDocTypeEnum {

    TEMP(1, "三测单"),
    NOTE(2, "护理记录单"),
    VITAL(3, "生命体征监测");

    private final int code;
    private final String label;

    NursingDocTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NursingDocTypeEnum fromCode(int code) {
        for (NursingDocTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        NursingDocTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
