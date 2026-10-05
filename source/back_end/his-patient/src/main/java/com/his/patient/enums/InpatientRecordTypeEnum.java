package com.his.patient.enums;

import lombok.Getter;

/**
 * 住院病历文书类型枚举
 */
@Getter
public enum InpatientRecordTypeEnum {

    ADMISSION(1, "入院记录"),
    FIRST_COURSE(2, "首次病程"),
    DAILY_COURSE(3, "日常病程"),
    PRE_OP_SUMMARY(4, "术前小结"),
    OPERATION_RECORD(5, "手术记录"),
    POST_OP_FIRST_COURSE(6, "术后首次病程"),
    DISCHARGE_RECORD(7, "出院记录"),
    DEATH_RECORD(8, "死亡记录"),
    CONSULTATION(9, "会诊记录"),
    TRANSFER(10, "转科记录"),
    TRANSFUSION(11, "输血记录");

    private final int code;
    private final String label;

    InpatientRecordTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InpatientRecordTypeEnum fromCode(int code) {
        for (InpatientRecordTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        InpatientRecordTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
