package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 单病种质控状态枚举
 */
@Getter
public enum SingleDiseaseQcStatusEnum {

    ABNORMAL(2, "异常"),
    PASS(1, "通过"),
    PENDING(0, "待质控");

    private final int code;
    private final String label;

    SingleDiseaseQcStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SingleDiseaseQcStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SingleDiseaseQcStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        SingleDiseaseQcStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        SingleDiseaseQcStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        SingleDiseaseQcStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
