package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 技术越权登记的来源单据类型
 */
@Getter
public enum TechOverrideSourceEnum {

    /**
     * 手术申请单
     */
    OPERATION_APPLY(1, "手术申请"),
    /**
     * 日间手术登记单
     */
    DAY_SURGERY(2, "日间手术"),
    /**
     * 住院医嘱主表（医嘱类别 6=手术医嘱）
     */
    INPATIENT_ORDER(3, "住院医嘱"),
    /**
     * 内镜检查记录
     */
    ENDOSCOPY(4, "内镜记录");

    private final int code;
    private final String label;

    TechOverrideSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static TechOverrideSourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TechOverrideSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        TechOverrideSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TechOverrideSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        TechOverrideSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
