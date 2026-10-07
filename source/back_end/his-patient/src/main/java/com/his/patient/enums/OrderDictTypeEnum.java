package com.his.patient.enums;

import com.his.common.constant.DictType;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 医嘱基础字典的三类口径（sql/142）：给药途径 / 用药频次 / 剂量单位。
 *
 * <p>码值→中文名的唯一出口（原 {@code OrderDictTypes} 的 {@code NAME} 映射已上移至此）。
 * 值域、中文名、以及「字典落在医嘱表的哪一列」三件事一起定义，避免统计查错列。
 */
@Getter
public enum OrderDictTypeEnum {

    ROUTE(DictType.ORDER_ROUTE, "给药途径", "route"),
    FREQ(DictType.ORDER_FREQ, "用药频次", "frequency"),
    DOSE_UNIT(DictType.DOSE_UNIT, "剂量单位", "dosage_unit");

    private final String type;
    private final String name;
    private final String orderColumn;

    OrderDictTypeEnum(String type, String name, String orderColumn) {
        this.type = type;
        this.name = name;
        this.orderColumn = orderColumn;
    }

    public static OrderDictTypeEnum fromCode(String type) {
        if (type == null) {
            return null;
        }
        for (OrderDictTypeEnum e : values()) {
            if (e.type.equals(type)) {
                return e;
            }
        }
        return null;
    }

    /** 是否为受管的医嘱字典类型 */
    public static boolean isManaged(String type) {
        return fromCode(type) != null;
    }

    public static String getText(String type) {
        if (type == null) {
            return "—";
        }
        OrderDictTypeEnum e = fromCode(type);
        return e != null ? e.name : "";
    }

    /**
     * 码值→异常 / 审计文案：不在三类之内返回「未知(type)」，保留原始值便于排查（绝不用于前端展示）。
     */
    public static String labelOrUnknown(String type) {
        if (type == null) {
            return "未知";
        }
        OrderDictTypeEnum e = fromCode(type);
        return e != null ? e.name : "未知(" + type + ")";
    }

    /** 医嘱表里对应的列名（只可能是三列之一，调用方不可传外部输入） */
    public static String orderColumn(String type) {
        OrderDictTypeEnum e = fromCode(type);
        return e != null ? e.orderColumn : null;
    }

    public static Map<String, String> all() {
        Map<String, String> m = new LinkedHashMap<>();
        for (OrderDictTypeEnum e : values()) {
            m.put(e.type, e.name);
        }
        return m;
    }
}
