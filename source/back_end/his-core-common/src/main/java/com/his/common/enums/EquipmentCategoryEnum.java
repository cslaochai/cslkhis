package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 设备类别（sys_equipment.category）：固定资产台账的设备大类。
 * <p>
 * 码值与文案是字典 his_equipment_category 的权威来源，改码值必须同步字典段与列注释。
 * 8-物流设备 是机器人这类院内运输设备，与医疗设备分开记，派单算法只认这一类。
 */
@Getter
public enum EquipmentCategoryEnum {

    LARGE_IMAGING(1, "大型影像设备"),
    LAB_ANALYSIS(2, "检验分析设备"),
    LIFE_SUPPORT(3, "生命支持设备"),
    OPERATING_ROOM(4, "手术室设备"),
    MONITOR_EMERGENCY(5, "监护急救设备"),
    REHAB(6, "康复理疗设备"),
    OTHER(7, "其他设备"),
    LOGISTICS(8, "物流设备");

    private final int code;
    private final String label;

    EquipmentCategoryEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EquipmentCategoryEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (EquipmentCategoryEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示：合法→label；null/脏值→空串，绝不返回 null
     */
    public static String getLabel(Integer code) {
        EquipmentCategoryEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    /**
     * 调用方自带缺省文案
     */
    public static String getLabel(Integer code, String defaultValue) {
        EquipmentCategoryEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    /**
     * 异常/审计/合规：null/脏值→未知(n)，保留原始码值便于排查，不喂前端展示
     */
    public static String getLabelOrUnknown(Integer code) {
        EquipmentCategoryEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * 是否物流设备（机器人）：只有这一类会被同步进物联网接入台账、参与派单
     */
    public static boolean isLogistics(Integer code) {
        return fromCode(code) == LOGISTICS;
    }
}
