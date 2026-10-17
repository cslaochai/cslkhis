package com.his.common.enums;

import com.his.common.util.TextUtil;
import lombok.Getter;

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

    public static DrugStockSourceTypeEnum fromCode(String code) {
        if (!TextUtil.hasText(code)) {
            return null;
        }
        String trimmed = code.trim();
        for (DrugStockSourceTypeEnum item : values()) {
            if (item.code.equals(trimmed)) {
                return item;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 与空串都不合法） */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    /**
     * 码值→展示文案。null 或不在枚举内（脏数据）返回空串 ""，绝不返回 null、不回落合法文案。
     */
    public static String getText(String code) {
        DrugStockSourceTypeEnum item = fromCode(code);
        return item != null ? item.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(String code) {
        DrugStockSourceTypeEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return !TextUtil.hasText(code) ? "未知" : "未知(" + code + ")";
    }
}
