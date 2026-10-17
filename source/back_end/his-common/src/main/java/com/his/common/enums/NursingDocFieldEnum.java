package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 护理文书字段中文名枚举（变更日志 fieldLabel 用，码值 = 文书表的列名）。
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
        if (null == code) {
            return null;
        }
        for (NursingDocFieldEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        if (null == code) {
            return "—";
        }
        NursingDocFieldEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        NursingDocFieldEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        NursingDocFieldEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
