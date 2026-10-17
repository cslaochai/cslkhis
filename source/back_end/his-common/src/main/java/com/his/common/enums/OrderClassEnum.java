package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 医嘱类别枚举
 */
@Getter
public enum OrderClassEnum {

    DRUG(1, "药品"),
    EXAMINATION(2, "检查"),
    LABORATORY(3, "检验"),
    TREATMENT(4, "治疗"),
    NURSING(5, "护理"),
    OPERATION(6, "手术"),
    TRANSFUSION(7, "输血"),
    MONITORING(8, "监护"),
    OTHER(9, "其他"),
    NUTRITION(10, "临床营养");

    private final int code;
    private final String label;

    OrderClassEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 是否为「药品」类别（药品才受用药安全 CDSS 约束、才要求剂量 / 途径齐全）
     */
    public static boolean isDrug(Integer code) {
        return code != null && code == DRUG.code;
    }

    public static OrderClassEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (OrderClassEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        OrderClassEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        OrderClassEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        OrderClassEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
