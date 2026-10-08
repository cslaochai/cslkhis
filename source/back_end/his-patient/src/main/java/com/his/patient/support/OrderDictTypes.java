package com.his.patient.support;

import com.his.common.constant.DictType;
import com.his.patient.enums.OrderDictTypeEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 医嘱基础字典的三类口径（sql/142）：给药途径 / 用药频次 / 剂量单位。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderDictTypes {

    /**
     * 给药途径
     */
    public static final String ROUTE = DictType.ORDER_ROUTE;
    /**
     * 用药频次
     */
    public static final String FREQ = DictType.ORDER_FREQ;
    /**
     * 剂量单位
     */
    public static final String DOSE_UNIT = DictType.DOSE_UNIT;
    public static final List<String> ALL = List.of(ROUTE, FREQ, DOSE_UNIT);

    /**
     * 类型中文名（<b>展示用</b>）。null 给「—」；不在三类之内返回空串，
     * 不回落到看似合法的类型名，也不暴露「未知(type)」—— 界面只说"没有文案"，脏值交由数据治理发现。
     */
    public static String text(String dictType) {
        return OrderDictTypeEnum.getText(dictType);
    }

    /**
     * 类型中文名（<b>异常 / 审计用</b>）：不在三类之内返回「未知(type)」，保留原始值便于排查。
     * 绝不用于前端展示。
     */
    public static String labelOrUnknown(String dictType) {
        return OrderDictTypeEnum.labelOrUnknown(dictType);
    }

    /**
     * 是否为受管的医嘱字典类型（决定能不能从这个口子写库）
     */
    public static boolean isManaged(String dictType) {
        return OrderDictTypeEnum.isManaged(dictType);
    }

    /**
     * 医嘱表里对应的列名（只可能是三列之一，调用方不可传外部输入）
     */
    public static String orderColumn(String dictType) {
        return OrderDictTypeEnum.orderColumn(dictType);
    }
}
