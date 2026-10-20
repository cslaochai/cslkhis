package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 护理质量检查单状态枚举（sql/168，落在护理质量检查单的状态列）
 */
@Getter
public enum NursingQcStatusEnum {

    /**
     * 草稿：可改明细
     */
    DRAFT(1, "草稿"),
    /**
     * 已确认：明细冻结，只能退回草稿后再改
     */
    CONFIRMED(2, "已确认");

    private final int code;
    private final String label;

    NursingQcStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static NursingQcStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (NursingQcStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        NursingQcStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        NursingQcStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        NursingQcStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
