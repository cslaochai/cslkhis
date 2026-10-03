package com.his.common.enums;

import lombok.Getter;

/**
 * 药品库存流水变动类型枚举（药品库存流水的变动类型列）
 *
 * <p>库存的一切增减都必须落一行流水（sql/45 的单一台账口径），本枚举就是那一列的字典权威。
 * <br>调拨是<b>成对的两行</b>（发出 7 为负、接收 8 为正），一张调拨单在流水里合计为 0 ——
 * 这正是"药只是换了地方、没多出也没少掉"的账面表达；少写一行就是凭空造药或抹药。
 * <br>字典码值同步在 {@code sql/154}（7/8/9）与 {@code sql/45}/{@code sql/127}（1~6）。
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
    /** 数量方向：1=入库（流水为正）、-1=出库（流水为负） */
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

    public static String labelOf(Integer code) {
        DrugStockChangeTypeEnum type = fromCode(code);
        return type == null ? "未知(" + code + ")" : type.getLabel();
    }

    /** 本类型是不是出库（落流水时 change_quantity 取负） */
    public boolean outbound() {
        return sign < 0;
    }
}
