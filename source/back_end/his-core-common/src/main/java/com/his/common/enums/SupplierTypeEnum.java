package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 供应商经营范围：一张全院往来单位主档（sys_supplier）里区分「这家供什么」。
 * <p>
 * 主数据层共用一张表（药企与器械厂商本来就会是同一批商业公司），资质层各自独立：
 * 药品走 GSP 首营企业，器械走产品注册证与生产/经营许可 —— 所以这里只标经营范围，不放资质字段。
 * <p>
 * 采购与设备建档的下拉按它过滤：药品采购只看 DRUG，设备档案只看 EQUIPMENT。
 */
@Getter
public enum SupplierTypeEnum {

    DRUG(1, "药品"),
    EQUIPMENT(2, "器械设备"),
    CONSUMABLE(3, "耗材"),
    LOGISTICS(4, "后勤物资");

    private final int code;
    private final String label;

    SupplierTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SupplierTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SupplierTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        SupplierTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        SupplierTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        SupplierTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
