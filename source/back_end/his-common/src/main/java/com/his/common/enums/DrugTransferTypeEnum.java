package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 药品调拨方向枚举（药品调拨单的调拨方向列，sql/154）
 */
@Getter
public enum DrugTransferTypeEnum {

    /**
     * 药库下拨药房：补充药房在架量
     */
    DOWN(1, "药库下拨药房", StockRoomEnum.WAREHOUSE, StockRoomEnum.PHARMACY),
    /**
     * 药房退回药库：滞销/近效期批次退回药库
     */
    RETURN(2, "药房退回药库", StockRoomEnum.PHARMACY, StockRoomEnum.WAREHOUSE);

    private final int code;
    private final String label;
    private final StockRoomEnum fromRoom;
    private final StockRoomEnum toRoom;

    DrugTransferTypeEnum(int code, String label, StockRoomEnum fromRoom, StockRoomEnum toRoom) {
        this.code = code;
        this.label = label;
        this.fromRoom = fromRoom;
        this.toRoom = toRoom;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static DrugTransferTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DrugTransferTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DrugTransferTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DrugTransferTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DrugTransferTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
