package com.his.common.enums;

import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 质控对象来自哪张表。
 */
@Getter
public enum QcRecordSourceEnum {

    /**
     * 门诊病历
     */
    OUTPATIENT("OUTPATIENT", "门诊病历", "biz_medical_record"),

    /**
     * 住院文书（住院病历文书）
     */
    INPATIENT("INPATIENT", "住院文书", "biz_inpatient_record");

    private final String code;

    private final String label;

    private final String tableName;

    QcRecordSourceEnum(String code, String label, String tableName) {
        this.code = code;
        this.label = label;
        this.tableName = tableName;
    }

    public static QcRecordSourceEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (QcRecordSourceEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        QcRecordSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        QcRecordSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        QcRecordSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
