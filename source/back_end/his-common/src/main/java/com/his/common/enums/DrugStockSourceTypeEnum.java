package com.his.common.enums;

import com.his.common.util.TextUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 药品库存流水来源单据类型枚举（字符串码值）。
 *
 * <p>写入方是各自动库存的单据服务，一单一码；新增一种会动库存的单据必须在这里补一条，
 * 不要回到处写裸字符串 —— 反查流水靠的就是这个串和来源单号对上。
 */
@Getter
public enum DrugStockSourceTypeEnum {

    MANUAL("manual", "手工维护"),
    DRUG_INBOUND("drugInbound", "药品入库"),
    STOCKTAKE("stocktake", "库存盘点"),
    DISPENSING("dispensing", "发药出库"),
    DISPENSE_RETURN("dispenseReturn", "退药回库"),
    WARD_DISPENSE("wardDispense", "病区摆药"),
    WARD_DISPENSE_RETURN("wardDispenseReturn", "病区摆药退回"),
    DRUG_TRANSFER("drugTransfer", "药品调拨"),
    SUPPLIER_RETURN("supplierReturn", "供应商退货");

    private final String code;
    private final String label;

    DrugStockSourceTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 与空串都不合法） */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    public static DrugStockSourceTypeEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (DrugStockSourceTypeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        DrugStockSourceTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        DrugStockSourceTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        DrugStockSourceTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
