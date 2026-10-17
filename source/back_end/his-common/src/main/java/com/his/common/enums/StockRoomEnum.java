package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 药品库存地点枚举（sql/154）
 */
@Getter
public enum StockRoomEnum {

    /**
     * 药库：整件存放，采购收货与供应商退货发生的库位
     */
    WAREHOUSE(1, "药库"),
    /**
     * 药房：在架发药，患者退药回到这里
     */
    PHARMACY(2, "药房");

    private final int code;
    private final String label;

    StockRoomEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static StockRoomEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (StockRoomEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        StockRoomEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        StockRoomEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        StockRoomEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
