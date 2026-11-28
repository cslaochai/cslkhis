package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 配送物品品类：同时承载「一箱装什么」与单箱基准重量 —— 本单总重由「品类 × 箱数」算出，不再由下单人手填。
 * <p>
 * code 就是库表 goods_type 与仿真报文的品类文本（「药品/标本/耗材/器械包/被服」），不另造数字码值，
 * 避免存量单据整体改口径。
 * <p>
 * 重量口径只有一条规则：<b>一箱 = 本院实际使用的那只周转箱/袋装到常规装载量</b>，不按件、不按单品规格算。
 * 数值按中型三甲实测装箱估算，规格描述（{@link #getSpec()}）就是给下单人和运营看的体感口径；
 * 实际装箱变了只改这里的两个数（spec 与 unitWeightKg），全链路（上限校验、总重、UI 提示）跟着变。
 * <p>
 * 额定载重 15kg 下各品类的箱数上限由 unitWeightKg 折算，见 {@code RobotDispatchConst#NOMINAL_PAYLOAD_KG}。
 */
public enum RobotGoodsTypeEnum {

    DRUG("药品", 3.6, "12 瓶 250ml 输液（周转箱半箱）"),
    SPECIMEN("标本", 2.0, "50 管试管（含转运架与冰袋）"),
    CONSUMABLE("耗材", 4.0, "敷料/注射器整箱（40×30×25 周转箱七分满）"),
    INSTRUMENT("器械包", 5.0, "单套手术器械（灭菌后含器械盒）"),
    LINEN("被服", 6.0, "2 套床单被套（一袋）");

    private final String code;
    private final double unitWeightKg;
    /** 一箱装什么：只用于展示，不参与计算 */
    private final String spec;

    RobotGoodsTypeEnum(String code, double unitWeightKg, String spec) {
        this.code = code;
        this.unitWeightKg = unitWeightKg;
        this.spec = spec;
    }

    public String getCode() {
        return code;
    }

    public double getUnitWeightKg() {
        return unitWeightKg;
    }

    public String getSpec() {
        return spec;
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
