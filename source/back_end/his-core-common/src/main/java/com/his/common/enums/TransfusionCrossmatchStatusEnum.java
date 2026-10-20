package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 配血状态枚举（与输血流程状态分离：配血不合时流程停在 0，但这条必须能看见）。
 */
@Getter
public enum TransfusionCrossmatchStatusEnum {

    PENDING(0, "待配血"),
    PARTIAL(1, "配血中（未配齐）"),
    ALL_MATCHED(2, "全部相合"),
    INCOMPATIBLE(3, "存在配血不合");

    private final int code;
    private final String label;

    TransfusionCrossmatchStatusEnum(int code, String label) {
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

    public static TransfusionCrossmatchStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TransfusionCrossmatchStatusEnum item : values()) {
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
        TransfusionCrossmatchStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TransfusionCrossmatchStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        TransfusionCrossmatchStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
