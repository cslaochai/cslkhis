package com.his.patient.enums;

import lombok.Getter;

/**
 * 护理文书字段中文名枚举（变更日志 fieldLabel 用，码 = biz_nursing_record 库列名）。
 */
@Getter
public enum NursingDocFieldEnum {

    NURSING_TYPE("nursing_type", "文书类型"),
    MEASURE_TIME("measure_time", "测量/记录时间"),
    SHIFT("shift", "班次"),
    TEMPERATURE("temperature", "体温"),
    PULSE("pulse", "脉搏"),
    RESPIRATION("respiration", "呼吸"),
    SYSTOLIC_PRESSURE("systolic_pressure", "收缩压"),
    DIASTOLIC_PRESSURE("diastolic_pressure", "舒张压"),
    SPO2("spo2", "血氧饱和度"),
    STOOL_COUNT("stool_count", "大便次数"),
    URINE_VOLUME("urine_volume", "尿量"),
    INTAKE_VOLUME("intake_volume", "入量"),
    OUTPUT_VOLUME("output_volume", "出量"),
    NURSING_LEVEL("nursing_level", "护理级别"),
    NURSING_CONTENT("nursing_content", "护理记录正文"),
    REMARK("remark", "备注");

    private final String code;
    private final String label;

    NursingDocFieldEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NursingDocFieldEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (NursingDocFieldEnum item : values()) {
            if (item.code.equals(code)) return item;
        }
        return null;
    }

    /**
     * 字段列名 → 中文文案（展示口径）。null →「—」；不在枚举内的列名<b>原样返回，不猜</b>（见类注释）。
     */
    public static String getText(String code) {
        if (code == null) {
            return "—";
        }
        NursingDocFieldEnum item = fromCode(code);
        return item == null ? code : item.label;
    }

    /**
     * 异常 / 审计用列名 → 文案。null →「未知」，不在枚举内返回「未知(列名)」，保留原始值以便排查。
     */
    public static String labelOrUnknown(String code) {
        NursingDocFieldEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
