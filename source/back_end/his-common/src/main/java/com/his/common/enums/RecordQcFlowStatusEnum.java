package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病历三级质控流转状态枚举
 */
@Getter
public enum RecordQcFlowStatusEnum {

    DEPT_PENDING(1, "科级待审"),
    ARCHIVE_PENDING(2, "病案室待审"),
    MEDAFFAIRS_PENDING(3, "医务处待审"),
    FINAL_APPROVED(4, "终审通过"),
    REWORKING(5, "整改中");

    private final int code;
    private final String label;

    RecordQcFlowStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RecordQcFlowStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RecordQcFlowStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        RecordQcFlowStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        RecordQcFlowStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RecordQcFlowStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
