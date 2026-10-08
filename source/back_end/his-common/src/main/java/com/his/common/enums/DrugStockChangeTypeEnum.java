package com.his.common.enums;

import lombok.Getter;

/**
 * 药品库存流水变动类型枚举（药品库存流水的变动类型列）
 */
@Getter
public enum DrugStockChangeTypeEnum {

    INBOUND(1, "入库", 1),
    DISPENSE_OUT(2, "发药出库", -1),
    RETURN_IN(3, "退药回库", 1),
    OTHER_OUT(4, "其他出库", -1),
    STOCKTAKE_PROFIT(5, "盘盈", 1),
    STOCKTAKE_LOSS(6, "盘亏", -1),
    TRANSFER_OUT(7, "调拨出库", -1),
    TRANSFER_IN(8, "调拨入库", 1),
    SUPPLIER_RETURN_OUT(9, "退货出库", -1);

    private final int code;
    private final String label;
    /**
     * 数量方向：1=入库（流水为正）、-1=出库（流水为负）
     */
    private final int sign;

    DrugStockChangeTypeEnum(int code, String label, int sign) {
        this.code = code;
        this.label = label;
        this.sign = sign;
    }

    public static DrugStockChangeTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DrugStockChangeTypeEnum type : values()) {
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
        DrugStockChangeTypeEnum type = fromCode(code);
        return type == null ? "未知(" + code + ")" : type.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        DrugStockChangeTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * 本类型是不是出库（落流水时 change_quantity 取负）
     */
    public boolean outbound() {
        return sign < 0;
    }
}
