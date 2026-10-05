package com.his.common.enums;

import lombok.Getter;

/**
 * 药品库存地点枚举（sql/154）
 *
 * <p>库存分两层是"退回"这件事成立的前提：只有药库和药房两个地点，
 * 才谈得上"药房把药退回药库"。历史上的单层库存里这句话无处落地（退给谁？回到哪一行？）。
 * <br>字典权威在本枚举，码值改动必须同步 {@code sql/154} 的 {@code his_stock_room} 段。
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

    public static String labelOf(Integer code) {
        StockRoomEnum room = fromCode(code);
        return room == null ? "未知(" + code + ")" : room.getLabel();
    }
}
