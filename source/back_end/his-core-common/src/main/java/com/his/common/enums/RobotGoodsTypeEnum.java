package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 配送物品品类：同时承载单箱基准重量 —— 本单总重由「品类 × 箱数」算出，不再由下单人手填。
 * <p>
 * code 就是库表 goods_type 与仿真报文的品类文本（「药品/标本/耗材/器械包/被服」），不另造数字码值，
 * 避免存量单据整体改口径。单箱重量按中型三甲实际装箱估算：液体输液箱 3.6kg、试管转运箱 2.0kg、
 * 耗材箱 4.0kg、灭菌器械包 5.0kg、被服袋 6.0kg。
 */
public enum RobotGoodsTypeEnum {

    DRUG("药品", 3.6),
    SPECIMEN("标本", 2.0),
    CONSUMABLE("耗材", 4.0),
    INSTRUMENT("器械包", 5.0),
    LINEN("被服", 6.0);

    private final String code;
    private final double unitWeightKg;

    RobotGoodsTypeEnum(String code, double unitWeightKg) {
        this.code = code;
        this.unitWeightKg = unitWeightKg;
    }

    public String getCode() {
        return code;
    }

    public double getUnitWeightKg() {
        return unitWeightKg;
    }

    /** 展示名与码值同文 */
    public String getLabel() {
        return code;
    }

    public static RobotGoodsTypeEnum fromCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (RobotGoodsTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(String code) {
        RobotGoodsTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.code;
    }

    public static String getLabel(String code, String defaultValue) {
        RobotGoodsTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.code;
    }

    public static String getLabelOrUnknown(String code) {
        RobotGoodsTypeEnum item = fromCode(code);
        return item == null ? (StringUtils.isBlank(code) ? "未知" : "未知(" + code + ")") : item.code;
    }

    /** 单箱基准重量；未知品类返回 null，由调用方报错，不做默认值兜底 */
    public static Double unitWeightOf(String code) {
        RobotGoodsTypeEnum item = fromCode(code);
        return item == null ? null : item.unitWeightKg;
    }
}
