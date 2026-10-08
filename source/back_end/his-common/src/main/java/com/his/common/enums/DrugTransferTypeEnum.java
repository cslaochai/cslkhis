package com.his.common.enums;

import lombok.Getter;

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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        DrugTransferTypeEnum type = fromCode(code);
        return type == null ? "未知(" + code + ")" : type.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        DrugTransferTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
