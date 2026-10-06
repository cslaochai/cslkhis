package com.his.patient.support;

import com.his.patient.enums.OrderDictTypeEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 医嘱基础字典的三类口径（sql/142）：给药途径 / 用药频次 / 剂量单位。
 *
 * <p>单点定义的目的：值域、中文名、以及「这个字典落在医嘱表的哪一列」三件事必须一起定义 ——
 * 分开写就会出现「字典加了值、使用量统计却查错列」这种静默失效（统计永远 0，页面看不出毛病）。
 *
 * <p><b>值域口径（不要各写一份）</b>：
 * 途径存中文（历史医嘱 route 列就是中文，改成码会让存量数据渲染成「未知」），
 * 频次存英文缩写（qd/bid…，与医嘱单书写习惯一致），剂量单位是字面单位（g/mg/ml/片…）。
 *
 * <p>码值→中文名见 {@link OrderDictTypeEnum}。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderDictTypes {

    /**
     * 给药途径
     */
    public static final String ROUTE = "his_order_route";
    /**
     * 用药频次
     */
    public static final String FREQ = "his_order_freq";
    /**
     * 剂量单位
     */
    public static final String DOSE_UNIT = "his_dose_unit";
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
