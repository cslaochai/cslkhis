package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 输血流程状态枚举（码值口径 = sql/39 列注释，前端筛选值必须逐一对齐）。
 */
@Getter
public enum TransfusionStatusEnum {

    PENDING_CROSSMATCH(0, "待配血"),
    CROSSMATCHED(1, "已配血"),
    ISSUED(2, "已发血"),
    INFUSING(3, "输注中"),
    FINISHED(4, "已完成"),
    CANCELLED(5, "已取消");

    private final int code;
    private final String label;

    TransfusionStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * Integer 码值判定：null 安全，语义同 == 比较 int 常量
     */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }

    public static TransfusionStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TransfusionStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        TransfusionStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TransfusionStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        TransfusionStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
