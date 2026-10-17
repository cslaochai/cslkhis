package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 血库出入库流水业务类型枚举
 */
@Getter
public enum BloodStockLogBizTypeEnum {

    INBOUND(1, "入库"),
    ISSUE(2, "发血"),
    RETURN(3, "退回"),
    SCRAP(4, "报废"),
    RESERVE(5, "预留"),
    CANCEL_RESERVE(6, "取消预留");

    private final int code;
    private final String label;

    BloodStockLogBizTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static BloodStockLogBizTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (BloodStockLogBizTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        BloodStockLogBizTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        BloodStockLogBizTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        BloodStockLogBizTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
