package com.his.common.enums;

import lombok.Getter;

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

    public static StockRoomEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StockRoomEnum room : values()) {
            if (room.code == code) {
                return room;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        StockRoomEnum room = fromCode(code);
        return room == null ? "未知(" + code + ")" : room.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        StockRoomEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
