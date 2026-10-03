package com.his.common.enums;

import lombok.Getter;

/**
 * 药品调拨方向枚举（药品调拨单的调拨方向列，sql/154）
 *
 * <p>方向决定 from/to 库位，所以单据上的 from_room / to_room 是派生值，不允许前端传。
 * <br>只做「药房退回药库」不做「药库下拨药房」是一条死路：药越退越堆在药库、药房退完就没法发药，
 * 最后还得靠人手工改库存补回去。所以这一族按双向设计。
 */
@Getter
public enum DrugTransferTypeEnum {

    /** 药库下拨药房：补充药房在架量 */
    DOWN(1, "药库下拨药房", StockRoomEnum.WAREHOUSE, StockRoomEnum.PHARMACY),
    /** 药房退回药库：滞销/近效期批次退回药库 */
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

    public static DrugTransferTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DrugTransferTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        DrugTransferTypeEnum type = fromCode(code);
        return type == null ? "未知(" + code + ")" : type.getLabel();
    }
}
